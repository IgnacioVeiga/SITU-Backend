-- Dev seed data.
-- Demo credentials (plain password is shared on purpose only in dev):
-- - johndoe@example.com / admin123456
-- - employee@example.com / admin123456
-- - passenger@example.com / admin123456
-- - tenantadmin@example.com / admin123456
-- BCrypt hash generated beforehand and intentionally reused in dev only.

INSERT INTO companies (id, name, logo_filename)
VALUES (1, 'SITU Demo', '/images/company_demo_logo.jpg'),
       (2, 'Transit QA', '/images/company_qa_logo.jpg');

INSERT INTO profile_images (id, filename)
VALUES (1, 'user_admin_demo.jpg'),
       (2, 'user_employee_demo.jpg'),
       (3, 'user_passenger_demo.jpg');

INSERT INTO users (id, company_id, profile_image_id, dni, first_name, last_name, role)
VALUES (1, 1, 1, 12345678, 'John', 'Doe', 'ADMIN'),
       (2, 1, 2, 22345678, 'Eva', 'Rivera', 'EMPLOYEE'),
       (3, 1, 3, 32345678, 'Pia', 'Gomez', 'PASSENGER'),
       (4, 2, NULL, 42345678, 'Luis', 'Tenant', 'ADMIN');

INSERT INTO user_credentials (user_id, email, password_hash)
VALUES (1, 'johndoe@example.com', '$2a$10$LZgvDbioLsZGXCmq.AH8TubH37f.TruxzaXZtuJR/YX8AdocWr1F.'), -- admin123456
       (2, 'employee@example.com', '$2a$10$LZgvDbioLsZGXCmq.AH8TubH37f.TruxzaXZtuJR/YX8AdocWr1F.'), -- admin123456
       (3, 'passenger@example.com', '$2a$10$LZgvDbioLsZGXCmq.AH8TubH37f.TruxzaXZtuJR/YX8AdocWr1F.'), -- admin123456
       (4, 'tenantadmin@example.com', '$2a$10$LZgvDbioLsZGXCmq.AH8TubH37f.TruxzaXZtuJR/YX8AdocWr1F.'); -- admin123456

INSERT INTO report_images (id, company_id, filename)
VALUES (1, 1, '/images/report_demo_1.jpg'),
       (2, 1, '/images/report_demo_2.jpg'),
       (3, 2, '/images/report_demo_tenant_2.jpg');

INSERT INTO lines (id, company_id, number, name)
VALUES (1, 1, '123', 'Linea 123'),
       (2, 1, '250', 'Linea 250'),
       (3, 2, '700', 'Linea 700');

INSERT INTO routes (id, line_id, name, coordinates)
VALUES (1, 1, 'Pilar - Palermo',
        ST_SetSRID(ST_MakeLine(ST_MakePoint(-58.914191, -34.458657), ST_MakePoint(-58.430314, -34.588045)), 4326)),
       (2, 2, 'Zarate - Once',
        ST_SetSRID(ST_MakeLine(ST_MakePoint(-59.028798, -34.097301), ST_MakePoint(-58.401184, -34.609880)), 4326)),
       (3, 3, 'Moron - Ituzaingo',
        ST_SetSRID(ST_MakeLine(ST_MakePoint(-58.619780, -34.653300), ST_MakePoint(-58.591600, -34.646000)), 4326));

INSERT INTO stops (id, name, location)
VALUES (1, 'Pilar', ST_SetSRID(ST_MakePoint(-58.914191, -34.458657), 4326)),
       (2, 'Palermo', ST_SetSRID(ST_MakePoint(-58.430314, -34.588045), 4326)),
       (3, 'Zarate', ST_SetSRID(ST_MakePoint(-59.028798, -34.097301), 4326)),
       (4, 'Once', ST_SetSRID(ST_MakePoint(-58.401184, -34.609880), 4326)),
       (5, 'Moron', ST_SetSRID(ST_MakePoint(-58.619780, -34.653300), 4326)),
       (6, 'Ituzaingo', ST_SetSRID(ST_MakePoint(-58.591600, -34.646000), 4326));

INSERT INTO routes_stops (route_id, stop_id, stop_order)
VALUES (1, 1, 1),
       (1, 2, 2),
       (2, 3, 1),
       (2, 4, 2),
       (3, 5, 1),
       (3, 6, 2);

INSERT INTO alerts (company_id, user_id, title, description, alert_date, starts_at, ends_at, is_active, priority, location)
VALUES (1, 1, 'Demora por corte de calle', 'Se reportan demoras por desvio temporal.', NOW(), NOW() - INTERVAL '30 minutes',
        NOW() + INTERVAL '6 hours', TRUE, 'HIGH', 'CABA'),
       (1, 2, 'Servicio reducido', 'Frecuencia reducida por congestion.', NOW(), NOW() - INTERVAL '1 hour',
        NOW() + INTERVAL '8 hours', TRUE, 'MEDIUM', 'AMBA'),
       (2, 4, 'Paro parcial', 'Se mantiene servicio minimo en franja nocturna.', NOW(), NOW() - INTERVAL '2 hours',
        NOW() + INTERVAL '3 hours', TRUE, 'LOW', 'Zona Oeste');

-- Complaint tracking tokens (dev only):
-- - SITU-DEV-COMP-001-TRACKING-ALPHA
-- - SITU-DEV-COMP-002-TRACKING-BRAVO
-- - SITU-DEV-COMP-003-TRACKING-CHARLIE
-- - SITU-DEV-COMP-004-TRACKING-DELTA
INSERT INTO complaints (
    id,
    company_id,
    reporter_user_id,
    assignee_user_id,
    report_image_id,
    description,
    reason,
    state,
    priority,
    is_anonymous,
    contact_email_encrypted,
    contact_phone_encrypted,
    tracking_token_encrypted,
    tracking_token_hash,
    created_at,
    updated_at,
    first_response_at,
    closed_at,
    response_due_at,
    resolution_due_at
)
VALUES (
           1,
           1,
           3,
           2,
           1,
           'La unidad no se detuvo en la parada solicitada durante el horario pico.',
           'Conduccion y frecuencia',
           'PENDING_REVIEW',
           'HIGH',
           TRUE,
           '7UeV4Fm2Cp3+V8ebZwRN5Fej2aUg9bL1Qwzs8x89fVbOyJJ05emC9ytaQ2lDyokd09vTlj3MqiTJ',
           'YMLnuJgaD2itIMb+3PXmTabHD/2Vm5/UrKdIJb4MBYb1yd4Yo31Tchm+P5A=',
           'Ys+Gt7b/JWt0B2Uphg4gv0tUpYTd+nap037KaTAsn599OEP/+D0q1Ag2foklZkfBcDloKGiw4zkhtJWr',
           '24aff09b4c4b46d1a70b0c2138f562d1ea389a196635404c3e5f4e7100a15534',
           NOW() - INTERVAL '5 days',
           NOW() - INTERVAL '5 days',
           NULL,
           NULL,
           NOW() - INTERVAL '4 days',
           NOW() + INTERVAL '2 days'
       ),
       (
           2,
           1,
           1,
           2,
           2,
           'El chofer no respeto el recorrido habitual y no hubo aviso previo.',
           'Recorrido alterado',
           'IN_REVIEW',
           'MEDIUM',
           FALSE,
           'w21Lglsewol/0w6YZyuM04JNVWBomet/WDjP3OXIU41/h3K7dpqv6Igl40yWtSCDSAGMup58I4u/',
           'ZrcZcU5vrXp1JJTQuJnxvTO3OOuX4cGBdNKKlGyXOMtPIjCUQL/7C4d8IB8=',
           'ujkgH0nuprj4csZftiTnvLs9Kw64md8kz/hMZOIlpAWaJ1X0XTE9xAs/nHpc6k2WT+UaOLRphy93xt4p',
           '3ed5d70f78c57e10b0bf362350a0b80bedf25a39fcc4ecc214ea22e1d236b762',
           NOW() - INTERVAL '3 days',
           NOW() - INTERVAL '1 day',
           NOW() - INTERVAL '2 days',
           NULL,
           NOW() - INTERVAL '1 day',
           NOW() + INTERVAL '10 days'
       ),
       (
           3,
           1,
           3,
           1,
           1,
           'Se reporta mala atencion al solicitar informacion sobre combinaciones.',
           'Atencion al pasajero',
           'CLOSED',
           'LOW',
           FALSE,
           'lAwpe4moYGrPLBKvnMWPGWUxrMynTa/XEjIH3FlPDVM5TX5CurqXnfbju7aQJVYm3FhAm+1FndiL',
           'Ve1QKsmuk5MIfrEFJ0rdLUS4qwnm+1pGodngxK0XO2nGVXmyx5MNlE75LVA=',
           'gxte1hZE9U1lycnQxd6Xn90qEDri6O5SyuLLQ/jT4xp2MMgXSrOeU7xhKEdg8zUaGK+RiHwOyYrNNbE0Y+s=',
           '14ecc1120a6e5b4b71037d3643d123c63c81f29d1219923490e5cacd7fe3d304',
           NOW() - INTERVAL '10 days',
           NOW() - INTERVAL '7 days',
           NOW() - INTERVAL '9 days',
           NOW() - INTERVAL '7 days',
           NOW() - INTERVAL '8 days',
           NOW() + INTERVAL '5 days'
       ),
       (
           4,
           2,
           4,
           4,
           3,
           'Se solicita revision de frecuencia nocturna por esperas prolongadas.',
           'Frecuencia nocturna',
           'PENDING_REVIEW',
           'MEDIUM',
           TRUE,
           'vHkTmPrE7ptlrgZHE4CkBvfnDuBXRdOsh3ukbFFJnzCj8ePCDIqtzAtO0SqFY8gFZY3dPlrBfGle',
           'rFYhcMQVJEGx7zcAsnOofROg1/qGYKVNeF/rtBYzMIgjIkNCA9Soj8+CdEY=',
           'TcLKY3V8Zbavx5uJkzbqZVL949VnT6l4CBUVn1ZJHhH13f0j5PrnpB0JmIPkxUJEhdrFcTc6HzViAkIR',
           '0e3f5dc0b6235fe3b48311c1d12454c3ac7d50f15949f8b10a0f3f7b4aded141',
           NOW() - INTERVAL '2 days',
           NOW() - INTERVAL '2 days',
           NULL,
           NULL,
           NOW() + INTERVAL '1 day',
           NOW() + INTERVAL '13 days'
       );

INSERT INTO complaints_lines (complaint_id, line_id)
VALUES (1, 1),
       (2, 2),
       (3, 1),
       (4, 3);

INSERT INTO complaints_routes (complaint_id, route_id)
VALUES (1, 1),
       (2, 2),
       (3, 1),
       (4, 3);

SELECT setval('companies_id_seq', COALESCE((SELECT MAX(id) FROM companies), 1), TRUE);
SELECT setval('profile_images_id_seq', COALESCE((SELECT MAX(id) FROM profile_images), 1), TRUE);
SELECT setval('users_id_seq', COALESCE((SELECT MAX(id) FROM users), 1), TRUE);
SELECT setval('user_credentials_id_seq', COALESCE((SELECT MAX(id) FROM user_credentials), 1), TRUE);
SELECT setval('report_images_id_seq', COALESCE((SELECT MAX(id) FROM report_images), 1), TRUE);
SELECT setval('lines_id_seq', COALESCE((SELECT MAX(id) FROM lines), 1), TRUE);
SELECT setval('routes_id_seq', COALESCE((SELECT MAX(id) FROM routes), 1), TRUE);
SELECT setval('stops_id_seq', COALESCE((SELECT MAX(id) FROM stops), 1), TRUE);
SELECT setval('alerts_id_seq', COALESCE((SELECT MAX(id) FROM alerts), 1), TRUE);
SELECT setval('complaints_id_seq', COALESCE((SELECT MAX(id) FROM complaints), 1), TRUE);
