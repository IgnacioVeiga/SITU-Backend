# Maps interoperability (PostGIS + Leaflet)

This document defines the geospatial contract between `SITU-Backend` and `SITU-Frontend`.

## 1. Storage model (backend)

- Database: PostgreSQL + PostGIS.
- SRID baseline: `4326` (WGS84).
- Tables:
  - `routes.coordinates` -> `GEOMETRY(LineString, 4326)`
  - `stops.location` -> `GEOMETRY(Point, 4326)`
- Spatial indexes:
  - `idx_routes_geom` (GiST)
  - `idx_stops_geom` (GiST)

## 2. Coordinate convention

- API write payloads use GeoJSON coordinate order:
  - `[longitude, latitude]`
- Leaflet renders in:
  - `[latitude, longitude]`
- Frontend must convert between both orders when sending and rendering.

## 3. Route write contract

Used by:
- `POST /api/v1/routes`
- `PUT /api/v1/routes/{id}`

DTO field:
- `coordinates` (string)

Expected value:
- Stringified GeoJSON `LineString` with at least 2 points.

Example:

```json
{
  "lineId": 1,
  "name": "Terminal - Centro",
  "coordinates": "{\"type\":\"LineString\",\"coordinates\":[[-58.3816,-34.6037],[-58.3951,-34.6082]]}"
}
```

Validation rules (server-side):
- payload must parse as JSON,
- `type` must be `LineString`,
- `coordinates` must be an array with at least 2 points,
- each point must contain longitude and latitude values.

Persistence:
- backend stores geometry with `ST_GeomFromGeoJSON(...)` + `ST_SetSRID(..., 4326)`.

## 4. Stop write contract

Used by:
- `POST /api/v1/stops`
- `PUT /api/v1/stops/{id}`

DTO field:
- `locationGeoJson` (string)

Expected value:
- Stringified GeoJSON `Point`.

Example:

```json
{
  "name": "Plaza Italia",
  "locationGeoJson": "{\"type\":\"Point\",\"coordinates\":[-58.4228,-34.5875]}"
}
```

## 5. Read-side format behavior

Current backend behavior:
- Routes are read from JPA geometry mapping and can appear as PostGIS/JDBC-native text (typically EWKB hex string).
- Stops are returned as GeoJSON text (`ST_AsGeoJSON` in repository queries).

Because of this, the frontend map parser supports both:
- GeoJSON text/object
- EWKB hex string

## 6. Frontend map editing flow (expected)

High-level flow in `/bus`:
1. select one or more lines,
2. select one or more routes to visualize,
3. select exactly one route for edit mode,
4. edit polyline vertices on Leaflet map,
5. send `PUT /routes/{id}` with updated GeoJSON `LineString`,
6. optionally undo changes before save.

## 7. Operational troubleshooting

Check geometry type and validity:

```sql
SELECT id, GeometryType(coordinates), ST_IsValid(coordinates)
FROM public.routes;
```

Render route as GeoJSON for debugging:

```sql
SELECT id, ST_AsGeoJSON(coordinates)
FROM public.routes
WHERE id = 1;
```

Check stop SRID consistency:

```sql
SELECT id, ST_SRID(location)
FROM public.stops
LIMIT 20;
```

If map rendering fails:
- verify route geometry is non-null and valid,
- verify route has at least 2 points,
- verify coordinate order in write payloads (`lon,lat`).
