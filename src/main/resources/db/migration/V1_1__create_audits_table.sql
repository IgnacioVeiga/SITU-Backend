CREATE TABLE audits (
    id SERIAL PRIMARY KEY,
    action VARCHAR(255) NOT NULL,      -- Action performed (e.g., CREATE_USER)
    username VARCHAR(255),             -- User who performed the action
    details TEXT,
    date TIMESTAMP NOT NULL DEFAULT NOW()
);
