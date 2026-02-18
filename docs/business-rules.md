# Business rules (current)

## Complaints
- `Complaint` is the canonical entity for user claims/reports.
- Main flow:
  - `PENDING_REVIEW`
  - `IN_REVIEW`
  - `CLOSED`
  - `REOPENED` (staff-only reopen path)

Allowed transitions are validated server-side.

## Complaint ownership and visibility
- Any authenticated user can create a complaint.
- Company staff (`ADMIN`, `SUPERVISOR`, `EMPLOYEE`) can:
  - list all complaints,
  - read complaint details,
  - update complaint status,
  - assign complaints.
- Any authenticated user can list their own complaints via `/complaints/mine/...`.
- Public tracking is available through token endpoint (`/complaints/tracking/{token}`).
- Staff complaint listing/detail endpoints are tenant-scoped: users can only read complaints from their own company.
- Complaint assignment is tenant-scoped: assignees must belong to the same company as the complaint.
- Related references (`lineIds`, `routeIds`, `stopIds`) are validated against the reporter company during creation.

## Anonymous complaints
- Anonymous mode is supported.
- Contact fields are encrypted at rest.
- API responses expose masked contact values only.

## Assignment
- Complaint assignment is explicit via `/complaints/{id}/assign`.
- On first status move to `IN_REVIEW`, if no assignee exists, current staff user becomes assignee.

## SLA defaults
- High priority:
  - first response due: 24h
  - resolution due: 7 days
- Medium/Low priority:
  - first response due: 72h
  - resolution due: 15 days

## Notifications
- On complaint state change, email notification is attempted if contact email exists.
- A mobile notification integration hook is already prepared in the service layer.

## Alerts
- Alerts are public notices and do not use a mandatory workflow state.
- Visibility is based on:
  - `isActive = true`
  - `startsAt <= now`
  - `endsAt is null or endsAt >= now`
- Any authenticated user can create alerts.
- Updates/deactivation are limited to company staff (`ADMIN`, `SUPERVISOR`, `EMPLOYEE`).
- Alert list and update operations are tenant-scoped by the authenticated user company.
