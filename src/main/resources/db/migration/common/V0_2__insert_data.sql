-- Inserting data

INSERT INTO companies (name, logo_filename)
VALUES
  ('Empresa A', '/images/company_a_logo.jpg'),
  ('Empresa B', '/images/company_b_logo.jpg'),
  ('Empresa C', '/images/company_c_logo.jpg');

INSERT INTO profile_images (filename)
VALUES
  ('123456789.jpg'),
  ('987654321.jpg'),
  ('555555555.jpg');

INSERT INTO report_images (filename)
VALUES
  ('/images/report_1_image.jpg'),
  ('/images/report_2_image.jpg'),
  ('/images/report_3_image.jpg');

INSERT INTO users (company_id, profile_image_id, dni, first_name, last_name, role)
VALUES
  (1, 1, 123456789, 'John', 'Doe', 'ADMIN'),
  (1, 2, 987654321, 'Jane', 'Doe', 'DRIVER'),
  (2, 3, 555555555, 'Bob', 'Smith', 'REGULAR');

INSERT INTO user_credentials (user_id, email, password_hash)
VALUES
  (1, 'johndoe@example.com',
  '$2a$10$w5qXpYFu8erYyxGQP7VnSuUK/427ssz3NJ0dwmqvpQ5ogOErLl2wC' -- hashed_password_1
  ),
  (2, 'janedoe@example.com',
   '$2a$10$yM8Eqb9DLyKMTlcCEnSDROt/il3RhBelJi4wa4RicvrqUY1zFu3s2' -- hashed_password_2
  ),
  (3, 'bobsmith@example.com',
  '$2a$10$j9S5n6Tcv7ZVdn5xAcLwD..ggfdVftNcBKeeFLghoIMxG8eFvDsti' -- hashed_password_3
  );

INSERT INTO alerts (user_id, title, description, alert_date, priority, location)
VALUES
  (1, 'Alerta 1', 'Descripción de la alerta 1', '2023-10-30 10:00:00', 'HIGH'::alert_priority, 'Ubicación 1'),
  (2, 'Alerta 2', 'Descripción de la alerta 2', '2023-10-30 11:00:00', 'MEDIUM'::alert_priority, 'Ubicación 2'),
  (3, 'Alerta 3', 'Descripción de la alerta 3', '2023-10-30 12:00:00', 'LOW'::alert_priority, 'Ubicación 3');

INSERT INTO reports (user_id, report_image_id, description, report_date, reason, state)
VALUES
  (1, 1, 'Descripción del reporte 1', '2023-10-29 12:00:00', 'Razón del reporte 1', 'WAITING'::report_state),
  (2, 2, 'Descripción del reporte 2', '2023-10-29 13:00:00', 'Razón del reporte 2', 'WORKING_ON_IT'::report_state),
  (3, 3, 'Descripción del reporte 3', '2023-10-29 14:00:00', 'Razón del reporte 3', 'RESOLVED'::report_state);

-- Inserting some bus lines for the existing company
INSERT INTO lines (number, name, company_id) VALUES
(123, '123', 1),
(250, '250', 1),
(500, '500', 1);

-- Insert routes for each line with coordinates
INSERT INTO routes (name, line_id, coordinates) VALUES
('Pilar - Palermo', 1, ST_SetSRID(ST_MakeLine(ST_MakePoint(-58.914191, -34.458657), ST_MakePoint(-58.430314, -34.588045)), 4326)), -- Pilar - Palermo
('Zarate - Once', 1, ST_SetSRID(ST_MakeLine(ST_MakePoint(-59.028798, -34.097301), ST_MakePoint(-58.401184, -34.609880)), 4326)),  -- Zarate - Once
('Lujan - Retiro', 1, ST_SetSRID(ST_MakeLine(ST_MakePoint(-59.103082, -34.566998), ST_MakePoint(-58.373892, -34.590442)), 4326)),  -- Lujan - Retiro
('San Fernando - Tigre', 2, ST_SetSRID(ST_MakeLine(ST_MakePoint(-58.546665, -34.448548), ST_MakePoint(-58.579599, -34.424947)), 4326)),  -- San Fernando - Tigre
('San Isidro - Victoria', 2, ST_SetSRID(ST_MakeLine(ST_MakePoint(-58.523641, -34.471748), ST_MakePoint(-58.558342, -34.451122)), 4326)),  -- San Isidro - Victoria
('Moron - Merlo', 3, ST_SetSRID(ST_MakeLine(ST_MakePoint(-58.619780, -34.653300), ST_MakePoint(-58.728260, -34.676391)), 4326)),  -- Moron - Merlo
('Moron - Ituzaingo', 3, ST_SetSRID(ST_MakeLine(ST_MakePoint(-58.619780, -34.653300), ST_MakePoint(-58.591600, -34.646000)), 4326));  -- Moron - Ituzaingó

-- Insert stops at different locations with coordinates in the location column
INSERT INTO stops (name, location) VALUES
('Pilar', ST_SetSRID(ST_MakePoint(-58.914191, -34.458657), 4326)),
('Palermo', ST_SetSRID(ST_MakePoint(-58.430314, -34.588045), 4326)),
('Zarate', ST_SetSRID(ST_MakePoint(-59.028798, -34.097301), 4326)),
('Once', ST_SetSRID(ST_MakePoint(-58.401184, -34.609880), 4326)),
('Lujan', ST_SetSRID(ST_MakePoint(-59.103082, -34.566998), 4326)),
('Retiro', ST_SetSRID(ST_MakePoint(-58.373892, -34.590442), 4326)),
('San Fernando', ST_SetSRID(ST_MakePoint(-58.546665, -34.448548), 4326)),
('Tigre', ST_SetSRID(ST_MakePoint(-58.579599, -34.424947), 4326)),
('San Isidro', ST_SetSRID(ST_MakePoint(-58.523641, -34.471748), 4326)),
('Victoria', ST_SetSRID(ST_MakePoint(-58.558342, -34.451122), 4326)),
('Moron', ST_SetSRID(ST_MakePoint(-58.619780, -34.653300), 4326)),
('Merlo', ST_SetSRID(ST_MakePoint(-58.728260, -34.676391), 4326)),
('Ituzaingó', ST_SetSRID(ST_MakePoint(-58.591600, -34.646000), 4326));

-- Associating stops to the routes (routes_stops)
INSERT INTO routes_stops (route_id, stop_id, stop_order) VALUES
(1, 1, 1), -- Pilar - Palermo: Parada Pilar (start)
(1, 2, 2), -- Pilar - Palermo: Parada Palermo (end)

(2, 3, 1), -- Zarate - Once: Parada Zarate (start)
(2, 4, 2), -- Zarate - Once: Parada Once (end)

(3, 5, 1), -- Lujan - Retiro: Parada Lujan (start)
(3, 6, 2), -- Lujan - Retiro: Parada Retiro (end)

(4, 7, 1), -- San Fernando - Tigre: Parada San Fernando (start)
(4, 8, 2), -- San Fernando - Tigre: Parada Tigre (end)

(5, 9, 1), -- San Isidro - Victoria: Parada San Isidro (start)
(5, 10, 2), -- San Isidro - Victoria: Parada Victoria (end)

(6, 11, 1), -- Moron - Merlo: Parada Moron (start)
(6, 12, 2), -- Moron - Merlo: Parada Merlo (end)

(7, 11, 1), -- Moron - Ituzaingó: Parada Moron (start)
(7, 13, 2); -- Moron - Ituzaingó: Parada Ituzaingó (end)
