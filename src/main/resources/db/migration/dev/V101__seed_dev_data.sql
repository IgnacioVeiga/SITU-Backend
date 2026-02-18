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

SELECT setval('companies_id_seq', COALESCE((SELECT MAX(id) FROM companies), 1), TRUE);
SELECT setval('profile_images_id_seq', COALESCE((SELECT MAX(id) FROM profile_images), 1), TRUE);
SELECT setval('users_id_seq', COALESCE((SELECT MAX(id) FROM users), 1), TRUE);
SELECT setval('user_credentials_id_seq', COALESCE((SELECT MAX(id) FROM user_credentials), 1), TRUE);
SELECT setval('report_images_id_seq', COALESCE((SELECT MAX(id) FROM report_images), 1), TRUE);
SELECT setval('lines_id_seq', COALESCE((SELECT MAX(id) FROM lines), 1), TRUE);
SELECT setval('routes_id_seq', COALESCE((SELECT MAX(id) FROM routes), 1), TRUE);
SELECT setval('stops_id_seq', COALESCE((SELECT MAX(id) FROM stops), 1), TRUE);
SELECT setval('alerts_id_seq', COALESCE((SELECT MAX(id) FROM alerts), 1), TRUE);
