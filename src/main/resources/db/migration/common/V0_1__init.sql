CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TYPE user_role AS ENUM ('ADMIN', 'DRIVER', 'REGULAR');
CREATE TYPE report_state AS ENUM ('WAITING', 'WORKING_ON_IT', 'RESOLVED');
CREATE TYPE alert_priority AS ENUM ('HIGH', 'MEDIUM', 'LOW');

CREATE TABLE companies (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  logo_filename VARCHAR(255)
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
  state report_state,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (report_image_id) REFERENCES report_images(id)
);

CREATE TABLE alerts (
  id SERIAL PRIMARY KEY,
  user_id INTEGER NOT NULL,
  title VARCHAR(255),
  description VARCHAR(255),
  alert_date TIMESTAMP,
  priority alert_priority,
  location VARCHAR(255),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create the “lines” table for bus lines
CREATE TABLE lines (
  id SERIAL PRIMARY KEY,
  company_id INTEGER REFERENCES companies(id),
  number VARCHAR(10) NOT NULL, -- Line number or code
  name VARCHAR(255) -- Descriptive name (optional)
);

-- Create the “routes” table for the routes of each bus line.
CREATE TABLE routes (
  id SERIAL PRIMARY KEY,
  line_id INTEGER REFERENCES lines(id),
  name VARCHAR(255) NOT NULL, -- Name of the route (e.g. Pilar - Palermo)
  coordinates GEOMETRY(LineString, 4326) -- Coordinates of the route
);

-- Create the “stops” table for bus stops
CREATE TABLE stops (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255),
  location GEOMETRY(Point, 4326) -- Coordinates of the stop
);

-- Create the intermediate table “route_stops” to associate stops to routes
CREATE TABLE routes_stops (
  route_id INTEGER REFERENCES routes(id),
  stop_id INTEGER REFERENCES stops(id),
  stop_order INTEGER, -- The order in which the stops appear on the route
  PRIMARY KEY (route_id, stop_id)
);

-- Create the “audits” table to log actions performed by users
CREATE TABLE audits (
    id SERIAL PRIMARY KEY,
    action VARCHAR(255) NOT NULL,      -- Action performed (e.g., CREATE_USER)
    username VARCHAR(255),             -- User who performed the action
    details TEXT,
    date TIMESTAMP NOT NULL DEFAULT NOW()
);
