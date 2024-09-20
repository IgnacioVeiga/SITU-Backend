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

-- Inserting some bus lines for the existing company
INSERT INTO lines (number, name, company_id) VALUES
(123, '123', 1),
(250, '250', 1),
(500, '500', 1);

-- Insert routes for each line
INSERT INTO routes (name, line_id) VALUES
('Pilar - Palermo', 1),
('Zarate - Once', 1),
('Lujan - Retiro', 1),
('San Fernando - Tigre', 2),
('San Isidro - Victoria', 2),
('Moron - Merlo', 3),
('Moron - Ituzaingo', 3);

-- Insert stops at different locations with coordinates in the location column
INSERT INTO stops (name, location) VALUES
('Parada Pilar', ST_SetSRID(ST_MakePoint(-58.914191, -34.458657), 4326)),
('Parada Palermo', ST_SetSRID(ST_MakePoint(-58.430314, -34.588045), 4326)),
('Parada Zarate', ST_SetSRID(ST_MakePoint(-59.028798, -34.097301), 4326)),
('Parada Once', ST_SetSRID(ST_MakePoint(-58.401184, -34.609880), 4326)),
('Parada Lujan', ST_SetSRID(ST_MakePoint(-59.103082, -34.566998), 4326)),
('Parada Retiro', ST_SetSRID(ST_MakePoint(-58.373892, -34.590442), 4326)),
('Parada San Fernando', ST_SetSRID(ST_MakePoint(-58.546665, -34.448548), 4326)),
('Parada Tigre', ST_SetSRID(ST_MakePoint(-58.579599, -34.424947), 4326)),
('Parada San Isidro', ST_SetSRID(ST_MakePoint(-58.523641, -34.471748), 4326)),
('Parada Victoria', ST_SetSRID(ST_MakePoint(-58.558342, -34.451122), 4326)),
('Parada Moron', ST_SetSRID(ST_MakePoint(-58.619780, -34.653300), 4326)),
('Parada Merlo', ST_SetSRID(ST_MakePoint(-58.728260, -34.676391), 4326)),
('Parada Ituzaingo', ST_SetSRID(ST_MakePoint(-58.591600, -34.646000), 4326));

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

(7, 11, 1), -- Moron - Ituzaingo: Parada Moron (start)
(7, 13, 2); -- Moron - Ituzaingo: Parada Ituzaingo (end)
