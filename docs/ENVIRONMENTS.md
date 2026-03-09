# Backend Environments

Environment conventions for `SITU-Backend`.

## Supported profiles

- `dev`
- `qa`
- `prod`

## Canonical variables reference

- `docs/environment.md`

## Local env file convention

- `.env.dev`
- `.env.qa`
- `.env.prod`

All are generated from `.env.example`.

## Shared rules

- Keep secrets out of Git (`.env.example` only is tracked).
- Keep runtime config aligned across:
  - `.env.example`
  - `application*.properties`
  - documentation under `docs/`

## Main categories

- profile: `SPRING_PROFILES_ACTIVE`, `APP_ENV`
- database: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- security: JWT + refresh cookie settings + CORS
- sensitive data: `DATA_ENCRYPTION_KEY`, `TRACKING_TOKEN_HASH_SECRET`
- mail: SMTP settings
- timezone/logging: `APP_TIMEZONE`, `LOG_SECURITY_LEVEL`

## Related docs

- `docs/environment.md`
- `docs/DEVELOPMENT_SETUP.md`
- `docs/TROUBLESHOOTING.md`