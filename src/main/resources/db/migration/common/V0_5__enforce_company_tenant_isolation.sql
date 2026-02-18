ALTER TABLE public.complaints
    ADD COLUMN IF NOT EXISTS company_id INTEGER;

ALTER TABLE public.alerts
    ADD COLUMN IF NOT EXISTS company_id INTEGER;

UPDATE public.complaints c
SET company_id = u.company_id
FROM public.users u
WHERE c.company_id IS NULL
  AND c.reporter_user_id = u.id;

UPDATE public.alerts a
SET company_id = u.company_id
FROM public.users u
WHERE a.company_id IS NULL
  AND a.user_id = u.id;

DO
$$
BEGIN
    IF EXISTS (SELECT 1 FROM public.complaints WHERE company_id IS NULL) THEN
        RAISE EXCEPTION 'Cannot enforce tenant isolation: complaints without company_id found';
    END IF;

    IF EXISTS (SELECT 1 FROM public.alerts WHERE company_id IS NULL) THEN
        RAISE EXCEPTION 'Cannot enforce tenant isolation: alerts without company_id found';
    END IF;
END
$$;

ALTER TABLE public.complaints
    ALTER COLUMN company_id SET NOT NULL;

ALTER TABLE public.alerts
    ALTER COLUMN company_id SET NOT NULL;

DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_complaints_company'
    ) THEN
        ALTER TABLE public.complaints
            ADD CONSTRAINT fk_complaints_company
                FOREIGN KEY (company_id) REFERENCES public.companies (id);
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_alerts_company'
    ) THEN
        ALTER TABLE public.alerts
            ADD CONSTRAINT fk_alerts_company
                FOREIGN KEY (company_id) REFERENCES public.companies (id);
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_complaints_company_created ON public.complaints (company_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_complaints_company_state ON public.complaints (company_id, state);
CREATE INDEX IF NOT EXISTS idx_complaints_company_reporter ON public.complaints (company_id, reporter_user_id);
CREATE INDEX IF NOT EXISTS idx_alerts_company_date ON public.alerts (company_id, alert_date DESC);
CREATE INDEX IF NOT EXISTS idx_alerts_company_active_dates ON public.alerts (company_id, is_active, starts_at, ends_at);
