# API v1 reference

Base path: `/api/v1`

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

Legacy compatibility:
- `/reports/...` maps to the same complaint controller.

## Alerts
- `GET /alerts/{pageIndex}/{pageSize}` (authenticated user, active/current alerts only)
- `POST /alerts` (authenticated user)
- `PATCH /alerts/{alertId}` (staff)

## Users
- `GET /users/{pageIndex}/{pageSize}/{companyId}` (management)
- `GET /users/{id}` (management)
- `POST /users` (management)

## Transit entities
- `GET /lines` (authenticated user)
- `GET /routes/line/{lineId}` (authenticated user)
- `GET /stops` (authenticated user)
- `GET /stops/route/{routeId}` (authenticated user)

Write operations on lines/routes/stops are restricted to management roles.

## Images
- `POST /images/upload/user-profile`
- `GET /images/user-profile/{filename}`

Image endpoints are currently staff-only.
