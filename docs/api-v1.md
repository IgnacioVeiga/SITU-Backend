# API v1 reference

Base path: `/api/v1`

All JSON endpoints use a common envelope:

```json
{
  "message": "optional-message-or-key",
  "data": {}
}
```

## Auth
- `POST /auth/login`
- `POST /auth/logout`
- `POST /auth/signup`
- `POST /auth/password`
- `GET /auth/session`

## Complaints
- `GET /complaints/{pageIndex}/{pageSize}` (staff)
- `GET /complaints/mine/{pageIndex}/{pageSize}` (authenticated user)
- `GET /complaints/{complaintId}` (staff)
- `GET /complaints/tracking/{trackingToken}` (public)
- `POST /complaints` (authenticated user)
- `PATCH /complaints/{complaintId}/state` (staff)
- `PATCH /complaints/{complaintId}/assign` (staff)

Tenant isolation:
- staff complaint endpoints are automatically scoped to the authenticated user's company.
- complaint assignment only allows assignees from the same company.

## Alerts
- `GET /alerts/{pageIndex}/{pageSize}` (authenticated user, active/current alerts only)
- `POST /alerts` (authenticated user)
- `PATCH /alerts/{alertId}` (staff)

Tenant isolation:
- alert list and update are automatically scoped to the authenticated user's company.

## Users
- `GET /users/{pageIndex}/{pageSize}` (management)
- `GET /users/{id}` (management)
- `POST /users` (management)
- `PUT /users/{id}` (management)
- `DELETE /users/{id}` (management)

## Transit entities
- `GET /lines` (authenticated user)
- `GET /routes/line/{lineId}` (authenticated user)
- `GET /stops` (authenticated user)
- `GET /stops/route/{routeId}` (authenticated user)

Write operations on lines/routes/stops are restricted to management roles.

## Images
- `POST /images/upload/user-profile`
- `GET /images/user-profile/{filename}`

Image upload endpoint is staff-only.
Image retrieval endpoint returns the binary resource directly (not wrapped in `ApiResponse`).
