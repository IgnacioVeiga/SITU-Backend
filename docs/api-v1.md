# API v1 reference

Base path: `/api/v1`

Successful JSON endpoints can use the common envelope:

```json
{
  "message": "optional-message-or-key",
  "data": {}
}
```

Error responses use a unified structure:

```json
{
  "timestamp": "2026-03-08T13:48:13.3040484Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "ERRORS.AUTH.INVALID_CREDENTIALS"
}
```

## Auth
- `POST /auth/login` (public)
- `POST /auth/logout` (public)
- `POST /auth/signup` (public)
- `POST /auth/password` (authenticated)
- `GET /auth/session` (authenticated)

Notes:
- Auth is cookie/JWT based (`authToken` cookie, HttpOnly).
- Frontend calls must include credentials (`withCredentials: true`).

## Company
- `GET /companies/me` (management roles)

## Complaints
- `GET /complaints/{pageIndex}/{pageSize}` (company staff)
- `GET /complaints/mine/{pageIndex}/{pageSize}` (authenticated user)
- `GET /complaints/{complaintId}` (company staff)
- `GET /complaints/tracking/{trackingToken}` (public)
- `POST /complaints` (authenticated user)
- `PATCH /complaints/{complaintId}/state` (company staff)
- `PATCH /complaints/{complaintId}/assign` (company staff)

Tenant isolation:
- Staff complaint endpoints are scoped to the authenticated user company.
- Assignment only allows assignees from the same company.
- Tracking token is intentionally omitted in staff-wide complaint responses.

## Alerts
- `GET /alerts/{pageIndex}/{pageSize}?activeOnly={true|false}` (authenticated user)
- `POST /alerts` (authenticated user)
- `PATCH /alerts/{alertId}` (company staff)

Notes:
- `activeOnly` defaults to `true`.
- List and update are tenant-scoped by authenticated company.

## Users
- `GET /users/{pageIndex}/{pageSize}` (management)
- `GET /users/{id}` (management)
- `POST /users` (management)
- `PUT /users/{id}` (management)
- `DELETE /users/{id}` (management)

## Transit entities (lines/routes/stops)

Read:
- `GET /lines` (authenticated user)
- `GET /lines/{id}` (authenticated user)
- `GET /routes/line/{lineId}` (authenticated user)
- `GET /routes/{id}` (authenticated user)
- `GET /stops` (authenticated user)
- `GET /stops/route/{routeId}` (authenticated user)

Write (management):
- `POST /lines`, `PUT /lines/{id}`, `DELETE /lines/{id}`
- `POST /routes`, `PUT /routes/{id}`, `DELETE /routes/{id}`
- `POST /stops`, `PUT /stops/{id}`, `DELETE /stops/{id}`

Geospatial payload contract:
- Route upsert expects:
  - `coordinates`: JSON string with GeoJSON `LineString`, at least 2 points.
  - coordinate order: `[longitude, latitude]`.
- Stop upsert expects:
  - `locationGeoJson`: JSON string with GeoJSON `Point`.
  - coordinate order: `[longitude, latitude]`.

Read format note:
- Route `coordinates` can come as PostGIS/JDBC-native geometry text (typically EWKB hex).
- Stop `location` is emitted as GeoJSON text (`ST_AsGeoJSON`).

Detailed map interoperability notes:
- `docs/maps-postgis-leaflet.md`

## Images
- `POST /images/upload/user-profile` (staff roles)
- `GET /images/user-profile/{filename}` (staff roles)

Notes:
- Upload response is `ApiResponse<Void>` (`data` is `null`).
- Image retrieval returns raw binary resource (`image/*`), not `ApiResponse`.
