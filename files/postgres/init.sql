CREATE TYPE user_role AS ENUM ('ADMIN', 'DRIVER', 'REGULAR');
CREATE TYPE report_state AS ENUM ('WAITING', 'WORKING_ON_IT', 'RESOLVED');
CREATE TYPE alert_priority AS ENUM ('HIGH', 'MEDIUM', 'LOW');

CREATE TABLE companies (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  logo_path VARCHAR(255) NOT NULL
);

CREATE TABLE profile_images (
  id SERIAL PRIMARY KEY,
  filename VARCHAR(255) NOT NULL
);

CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  company_id INTEGER,
  profile_image_id INTEGER,
  dni INTEGER,
  first_name VARCHAR(255),
  last_name VARCHAR(255),
  role user_role,
  FOREIGN KEY (company_id) REFERENCES companies(id),
  FOREIGN KEY (profile_image_id) REFERENCES profile_images(id)
);

CREATE TABLE user_credentials (
  id SERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE report_images (
  id SERIAL PRIMARY KEY,
  filename VARCHAR(255) NOT NULL
);

CREATE TABLE reports (
  id SERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL,
  report_image_id INTEGER,
  description VARCHAR(255),
  report_date TIMESTAMP,
  reason VARCHAR(255),
  state INTEGER,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (report_image_id) REFERENCES report_images(id)
);

CREATE TABLE alerts (
  id SERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL,
  title VARCHAR(255),
  description VARCHAR(255),
  alert_date TIMESTAMP,
  priority INTEGER,
  location VARCHAR(255),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO companies (name, logo_path)
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
  ('/images/report_2_image.jpg');

INSERT INTO users (company_id, profile_image_id, dni, first_name, last_name, role)
VALUES
  (1, 1, 123456789, 'John', 'Doe', 'ADMIN'),
  (1, 2, 987654321, 'Jane', 'Doe', 'DRIVER'),
  (2, 3, 555555555, 'Bob', 'Smith', 'REGULAR');

INSERT INTO user_credentials (user_id, email, password_hash)
VALUES
  (1, 'johndoe@example.com', 'hashed_password_1'),
  (2, 'janedoe@example.com', 'hashed_password_2'),
  (3, 'bobsmith@example.com', 'hashed_password_3');

INSERT INTO alerts (user_id, title, description, alert_date, priority, location)
VALUES
  (1, 'Alerta 1', 'Descripción de la alerta 1', '2023-10-30 10:00:00', 1, 'Ubicación 1'),
  (2, 'Alerta 2', 'Descripción de la alerta 2', '2023-10-30 11:00:00', 2, 'Ubicación 2');

INSERT INTO reports (user_id, report_image_id, description, report_date, reason, state)
VALUES
  (1, 1, 'Descripción del reporte 1', '2023-10-29 12:00:00', 'Razón del reporte 1', 1),
  (2, 2, 'Descripción del reporte 2', '2023-10-29 13:00:00', 'Razón del reporte 2', 2);