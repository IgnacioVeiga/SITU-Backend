ALTER TABLE alerts ADD COLUMN temp_alert_priority alert_priority;

UPDATE alerts SET temp_alert_priority =
    CASE
        WHEN priority = 1 THEN 'HIGH'::alert_priority
        WHEN priority = 2 THEN 'MEDIUM'::alert_priority
        WHEN priority = 3 THEN 'LOW'::alert_priority
        ELSE NULL
    END;

ALTER TABLE alerts DROP COLUMN priority;
ALTER TABLE alerts RENAME COLUMN temp_alert_priority TO priority;

ALTER TABLE reports ADD COLUMN temp_report_state report_state;

UPDATE reports SET temp_report_state =
    CASE
        WHEN state = 1 THEN 'WAITING'::report_state
        WHEN state = 2 THEN 'WORKING_ON_IT'::report_state
        WHEN state = 3 THEN 'RESOLVED'::report_state
        ELSE NULL
    END;

ALTER TABLE reports DROP COLUMN state;
ALTER TABLE reports RENAME COLUMN temp_report_state TO state;
