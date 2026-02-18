# LLM Coding Instructions - SITU Backend

Machine-oriented guidance for automated code changes in `SITU-Backend`.
Human onboarding/operations docs are in `docs/`.

## 1. Repository purpose

- Backend API for complaint and alert management in public transport companies.
- Stack: Java 21, Spring Boot 3.5.x, Spring Security, Spring Data JPA, Flyway, PostgreSQL/PostGIS.
- API base path: `/api/v1`.
- CI trigger: runs on commits to `main` (`.github/workflows/ci.yml`).

## 2. Environment model

- Profiles: `dev`, `qa`, `prod`.
- Do not add a backend `mock` profile.
- Flyway layout is `common + env`:
  - `db/migration/common`
  - `db/migration/dev`
  - `db/migration/qa`
  - `db/migration/prod`

## 3. Non-negotiable architecture rules

- Keep Flyway as the only schema evolution mechanism.
  - Never rely on Hibernate auto-DDL for schema changes.
- Keep controller contracts DTO-based.
  - Do not return JPA entities directly from controllers.
- Keep API response shape consistent (global error payload and `ApiResponse<T>` where already used).
- Keep auth stateless and cookie/JWT based.

## 4. Security and data rules

- Keep authentication endpoints behavior aligned with frontend integration (`withCredentials`).
- Preserve role boundaries for staff-only operations (`ADMIN`, `SUPERVISOR`, `EMPLOYEE`).
- Complaint contact data is encrypted at rest.
  - `security.data.encryption-key` must remain configurable from environment.
- Anonymous complaint mode must keep contact masking for non-owner views.

## 5. Configuration and secrets policy

- Keep sensitive values out of Git.
  - Track only `.env.example`.
  - Never commit `.env.dev`, `.env.qa`, `.env.prod`.
- If runtime vars change, update in the same change:
  1. `application*.properties`
  2. `.env.example`
  3. `docs/environment.md`

## 6. Change policy for LLM edits

- Prefer small, localized edits over broad rewrites.
- Keep business rules explicit in service layer.
- Add/update tests when behavior changes.
- If API contracts change, update docs and frontend assumptions in parallel.

## 7. Verification checklist

Run when relevant:

- `./mvnw test`
- `./mvnw -DskipTests package`

For migration/auth changes, also verify startup and Flyway execution.

## 8. Read these files first

- `README.md`
- `docs/environment.md`
- `docs/migrations.md`
- `docs/business-rules.md`
- `docs/api-v1.md`
