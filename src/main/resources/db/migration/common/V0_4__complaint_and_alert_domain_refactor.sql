DO
$$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'complaint_state') THEN
        CREATE TYPE complaint_state AS ENUM ('PENDING_REVIEW', 'IN_REVIEW', 'CLOSED', 'REOPENED');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'complaint_priority') THEN
        CREATE TYPE complaint_priority AS ENUM ('HIGH', 'MEDIUM', 'LOW');
    END IF;
END
$$;

DO
$$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'reports'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'complaints'
    ) THEN
        ALTER TABLE public.reports RENAME TO complaints;
    END IF;
END
$$;

DO
$$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'complaints' AND column_name = 'user_id'
    ) THEN
        ALTER TABLE public.complaints RENAME COLUMN user_id TO reporter_user_id;
    END IF;

    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'complaints' AND column_name = 'report_date'
    ) THEN
        ALTER TABLE public.complaints RENAME COLUMN report_date TO created_at;
    END IF;
END
$$;

ALTER TABLE public.complaints
    ADD COLUMN IF NOT EXISTS assignee_user_id INTEGER,
    ADD COLUMN IF NOT EXISTS is_anonymous BOOLEAN,
    ADD COLUMN IF NOT EXISTS contact_email_encrypted TEXT,
    ADD COLUMN IF NOT EXISTS contact_phone_encrypted TEXT,
    ADD COLUMN IF NOT EXISTS tracking_token VARCHAR(64),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS first_response_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS closed_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS response_due_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS resolution_due_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS priority complaint_priority;

DO
$$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'complaints'
          AND column_name = 'state'
    ) THEN
        ALTER TABLE public.complaints ADD COLUMN IF NOT EXISTS state_v2 complaint_state;

        UPDATE public.complaints
        SET state_v2 = CASE COALESCE(state::TEXT, '')
                           WHEN 'WAITING' THEN 'PENDING_REVIEW'::complaint_state
                           WHEN 'WORKING_ON_IT' THEN 'IN_REVIEW'::complaint_state
                           WHEN 'RESOLVED' THEN 'CLOSED'::complaint_state
                           WHEN 'PENDING_REVIEW' THEN 'PENDING_REVIEW'::complaint_state
                           WHEN 'IN_REVIEW' THEN 'IN_REVIEW'::complaint_state
                           WHEN 'CLOSED' THEN 'CLOSED'::complaint_state
                           WHEN 'REOPENED' THEN 'REOPENED'::complaint_state
                           ELSE 'PENDING_REVIEW'::complaint_state
            END
        WHERE state_v2 IS NULL;

        ALTER TABLE public.complaints DROP COLUMN state;
        ALTER TABLE public.complaints RENAME COLUMN state_v2 TO state;
    END IF;
END
$$;

DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_complaints_reporter_user'
    ) THEN
        ALTER TABLE public.complaints
            ADD CONSTRAINT fk_complaints_reporter_user
                FOREIGN KEY (reporter_user_id) REFERENCES public.users (id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_complaints_assignee_user'
    ) THEN
        ALTER TABLE public.complaints
            ADD CONSTRAINT fk_complaints_assignee_user
                FOREIGN KEY (assignee_user_id) REFERENCES public.users (id);
    END IF;
END
$$;

UPDATE public.complaints
SET is_anonymous = COALESCE(is_anonymous, FALSE),
    updated_at = COALESCE(updated_at, created_at, NOW()),
    created_at = COALESCE(created_at, NOW()),
    tracking_token = COALESCE(tracking_token, UPPER(MD5(RANDOM()::TEXT || CLOCK_TIMESTAMP()::TEXT || id::TEXT))),
    priority = COALESCE(priority, 'MEDIUM'::complaint_priority),
    response_due_at = COALESCE(
            response_due_at,
            COALESCE(created_at, NOW()) + INTERVAL '72 hours'
                      ),
    resolution_due_at = COALESCE(
            resolution_due_at,
            COALESCE(created_at, NOW()) + INTERVAL '15 days'
                        );

ALTER TABLE public.complaints
    ALTER COLUMN state SET DEFAULT 'PENDING_REVIEW'::complaint_state,
    ALTER COLUMN state SET NOT NULL,
    ALTER COLUMN priority SET DEFAULT 'MEDIUM'::complaint_priority,
    ALTER COLUMN priority SET NOT NULL,
    ALTER COLUMN is_anonymous SET DEFAULT FALSE,
    ALTER COLUMN is_anonymous SET NOT NULL,
    ALTER COLUMN tracking_token SET NOT NULL,
    ALTER COLUMN created_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL,
    ALTER COLUMN response_due_at SET NOT NULL,
    ALTER COLUMN resolution_due_at SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_complaints_tracking_token ON public.complaints (tracking_token);
CREATE INDEX IF NOT EXISTS idx_complaints_state ON public.complaints (state);
CREATE INDEX IF NOT EXISTS idx_complaints_priority ON public.complaints (priority);
CREATE INDEX IF NOT EXISTS idx_complaints_reporter ON public.complaints (reporter_user_id);
CREATE INDEX IF NOT EXISTS idx_complaints_assignee ON public.complaints (assignee_user_id);

CREATE TABLE IF NOT EXISTS public.complaints_lines
(
    complaint_id INTEGER NOT NULL REFERENCES public.complaints (id),
    line_id      INTEGER NOT NULL REFERENCES public.lines (id),
    PRIMARY KEY (complaint_id, line_id)
);

CREATE TABLE IF NOT EXISTS public.complaints_routes
(
    complaint_id INTEGER NOT NULL REFERENCES public.complaints (id),
    route_id     INTEGER NOT NULL REFERENCES public.routes (id),
    PRIMARY KEY (complaint_id, route_id)
);

CREATE TABLE IF NOT EXISTS public.complaints_stops
(
    complaint_id INTEGER NOT NULL REFERENCES public.complaints (id),
    stop_id      INTEGER NOT NULL REFERENCES public.stops (id),
    PRIMARY KEY (complaint_id, stop_id)
);

ALTER TABLE public.alerts
    ADD COLUMN IF NOT EXISTS starts_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS ends_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN;

UPDATE public.alerts
SET starts_at = COALESCE(starts_at, alert_date, NOW()),
    is_active = COALESCE(is_active, TRUE),
    alert_date = COALESCE(alert_date, starts_at, NOW());

ALTER TABLE public.alerts
    ALTER COLUMN starts_at SET NOT NULL,
    ALTER COLUMN is_active SET DEFAULT TRUE,
    ALTER COLUMN is_active SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_alerts_active_dates ON public.alerts (is_active, starts_at, ends_at);
