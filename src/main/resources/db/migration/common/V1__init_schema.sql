CREATE EXTENSION IF NOT EXISTS postgis;

DO
$$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE user_role AS ENUM ('ADMIN', 'SUPERVISOR', 'EMPLOYEE', 'DRIVER', 'PASSENGER', 'REGULAR');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'alert_priority') THEN
        CREATE TYPE alert_priority AS ENUM ('HIGH', 'MEDIUM', 'LOW');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'complaint_state') THEN
        CREATE TYPE complaint_state AS ENUM ('PENDING_REVIEW', 'IN_REVIEW', 'CLOSED', 'REOPENED');
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'complaint_priority') THEN
        CREATE TYPE complaint_priority AS ENUM ('HIGH', 'MEDIUM', 'LOW');
    END IF;
END
$$;

CREATE TABLE companies
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL UNIQUE,
    logo_filename VARCHAR(255)
);

CREATE TABLE profile_images
(
    id       BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL
);

CREATE TABLE users
(
    id               BIGSERIAL PRIMARY KEY,
    company_id       BIGINT      NOT NULL REFERENCES companies (id),
    profile_image_id BIGINT REFERENCES profile_images (id),
    dni              INTEGER     NOT NULL,
    first_name       VARCHAR(255) NOT NULL,
    last_name        VARCHAR(255) NOT NULL,
    role             user_role    NOT NULL
);

ALTER TABLE users
    ADD CONSTRAINT uq_users_company_dni UNIQUE (company_id, dni);

ALTER TABLE users
    ADD CONSTRAINT uq_users_id_company UNIQUE (id, company_id);

CREATE TABLE user_credentials
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT       NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE report_images
(
    id         BIGSERIAL PRIMARY KEY,
    company_id BIGINT       NOT NULL REFERENCES companies (id),
    filename   VARCHAR(255) NOT NULL
);

ALTER TABLE report_images
    ADD CONSTRAINT uq_report_images_id_company UNIQUE (id, company_id);

CREATE TABLE lines
(
    id         BIGSERIAL PRIMARY KEY,
    company_id BIGINT      NOT NULL REFERENCES companies (id),
    number     VARCHAR(20) NOT NULL,
    name       VARCHAR(255)
);

CREATE UNIQUE INDEX uq_lines_company_number_ci ON lines (company_id, LOWER(number));

CREATE TABLE routes
(
    id          BIGSERIAL PRIMARY KEY,
    line_id     BIGINT       NOT NULL REFERENCES lines (id),
    name        VARCHAR(255) NOT NULL,
    coordinates GEOMETRY(LineString, 4326)
);

CREATE INDEX idx_routes_line ON routes (line_id);
CREATE INDEX idx_routes_geom ON routes USING GIST (coordinates);

CREATE TABLE stops
(
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    location GEOMETRY(Point, 4326)
);

CREATE INDEX idx_stops_geom ON stops USING GIST (location);

CREATE TABLE routes_stops
(
    route_id   BIGINT  NOT NULL REFERENCES routes (id) ON DELETE CASCADE,
    stop_id    BIGINT  NOT NULL REFERENCES stops (id),
    stop_order INTEGER NOT NULL,
    PRIMARY KEY (route_id, stop_id)
);

CREATE INDEX idx_routes_stops_order ON routes_stops (route_id, stop_order);

CREATE TABLE alerts
(
    id          BIGSERIAL PRIMARY KEY,
    company_id  BIGINT            NOT NULL REFERENCES companies (id),
    user_id     BIGINT            NOT NULL,
    title       VARCHAR(255)      NOT NULL,
    description TEXT              NOT NULL,
    alert_date  TIMESTAMP         NOT NULL,
    starts_at   TIMESTAMP         NOT NULL,
    ends_at     TIMESTAMP,
    is_active   BOOLEAN           NOT NULL DEFAULT TRUE,
    priority    alert_priority    NOT NULL DEFAULT 'MEDIUM',
    location    VARCHAR(255),
    CONSTRAINT ck_alert_window CHECK (ends_at IS NULL OR ends_at >= starts_at),
    CONSTRAINT fk_alerts_user_company FOREIGN KEY (user_id, company_id) REFERENCES users (id, company_id)
);

CREATE INDEX idx_alerts_company_date ON alerts (company_id, alert_date DESC);
CREATE INDEX idx_alerts_company_active_dates ON alerts (company_id, is_active, starts_at, ends_at);

CREATE TABLE complaints
(
    id                       BIGSERIAL PRIMARY KEY,
    company_id               BIGINT             NOT NULL REFERENCES companies (id),
    reporter_user_id         BIGINT             NOT NULL,
    assignee_user_id         BIGINT,
    report_image_id          BIGINT,
    description              TEXT               NOT NULL,
    reason                   VARCHAR(255),
    state                    complaint_state    NOT NULL DEFAULT 'PENDING_REVIEW',
    priority                 complaint_priority NOT NULL DEFAULT 'MEDIUM',
    is_anonymous             BOOLEAN            NOT NULL DEFAULT FALSE,
    contact_email_encrypted  TEXT               NOT NULL,
    contact_phone_encrypted  TEXT               NOT NULL,
    tracking_token_encrypted TEXT               NOT NULL,
    tracking_token_hash      VARCHAR(64)        NOT NULL,
    created_at               TIMESTAMP          NOT NULL,
    updated_at               TIMESTAMP          NOT NULL,
    first_response_at        TIMESTAMP,
    closed_at                TIMESTAMP,
    response_due_at          TIMESTAMP          NOT NULL,
    resolution_due_at        TIMESTAMP          NOT NULL,
    CONSTRAINT fk_complaints_reporter_company FOREIGN KEY (reporter_user_id, company_id) REFERENCES users (id, company_id),
    CONSTRAINT fk_complaints_assignee_company FOREIGN KEY (assignee_user_id, company_id) REFERENCES users (id, company_id),
    CONSTRAINT fk_complaints_image_company FOREIGN KEY (report_image_id, company_id) REFERENCES report_images (id, company_id),
    CONSTRAINT uq_complaints_tracking_hash UNIQUE (tracking_token_hash),
    CONSTRAINT ck_complaint_dates CHECK (updated_at >= created_at)
);

CREATE INDEX idx_complaints_company_created ON complaints (company_id, created_at DESC);
CREATE INDEX idx_complaints_company_state ON complaints (company_id, state);
CREATE INDEX idx_complaints_company_reporter ON complaints (company_id, reporter_user_id);
CREATE INDEX idx_complaints_company_assignee ON complaints (company_id, assignee_user_id);

CREATE TABLE complaints_lines
(
    complaint_id BIGINT NOT NULL REFERENCES complaints (id) ON DELETE CASCADE,
    line_id      BIGINT NOT NULL REFERENCES lines (id),
    PRIMARY KEY (complaint_id, line_id)
);

CREATE TABLE complaints_routes
(
    complaint_id BIGINT NOT NULL REFERENCES complaints (id) ON DELETE CASCADE,
    route_id     BIGINT NOT NULL REFERENCES routes (id),
    PRIMARY KEY (complaint_id, route_id)
);

CREATE TABLE complaints_stops
(
    complaint_id BIGINT NOT NULL REFERENCES complaints (id) ON DELETE CASCADE,
    stop_id      BIGINT NOT NULL REFERENCES stops (id),
    PRIMARY KEY (complaint_id, stop_id)
);

CREATE TABLE audits
(
    id         BIGSERIAL PRIMARY KEY,
    company_id BIGINT REFERENCES companies (id),
    action     VARCHAR(64)  NOT NULL,
    username   VARCHAR(255),
    details    TEXT,
    date       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audits_company_date ON audits (company_id, date DESC);
