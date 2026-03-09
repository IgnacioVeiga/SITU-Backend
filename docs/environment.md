# Backend environment variables

Canonical runtime variable reference for `SITU-Backend`.

## Profile selection

```env
SPRING_PROFILES_ACTIVE=dev
APP_ENV=dev
```

- Supported profiles: `dev`, `qa`, `prod`.
- `APP_ENV` is optional and mirrors profile information for app-level telemetry/logging.

## Database

```env
DB_URL=jdbc:postgresql://localhost:5432/situ_dev
DB_USERNAME=user_admin
DB_PASSWORD=admin@123
```

## JWT access token

```env
JWT_SECRET=replace-with-a-strong-secret-min-32-bytes
JWT_ACCESS_EXPIRATION_SECONDS=900
```

## Refresh session and cookie

```env
AUTH_REFRESH_EXPIRATION_SECONDS=43200
AUTH_REFRESH_REMEMBER_EXPIRATION_SECONDS=2592000

AUTH_REFRESH_COOKIE_NAME=situ_refresh_token
AUTH_REFRESH_COOKIE_PATH=/api/v1/auth
AUTH_REFRESH_COOKIE_SAME_SITE=Lax
AUTH_REFRESH_COOKIE_SECURE=false
AUTH_REFRESH_COOKIE_DOMAIN=
```

Rules:

- `AUTH_REFRESH_REMEMBER_EXPIRATION_SECONDS` must be greater than or equal to `AUTH_REFRESH_EXPIRATION_SECONDS`.
- If `AUTH_REFRESH_COOKIE_SAME_SITE=None`, `AUTH_REFRESH_COOKIE_SECURE` must be `true`.

## CORS

```env
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

Use a comma-separated list for multiple origins.

## Sensitive complaint data

```env
DATA_ENCRYPTION_KEY=replace-with-a-strong-encryption-key
TRACKING_TOKEN_HASH_SECRET=replace-with-a-different-strong-secret
```

- `DATA_ENCRYPTION_KEY` encrypts complaint contact fields at rest.
- `TRACKING_TOKEN_HASH_SECRET` hashes tracking tokens (HMAC-SHA256).

## Mail

```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=admin@example.com
MAIL_PASSWORD=app-password-or-token
```

## Logging, metrics and timezone

```env
LOG_SECURITY_LEVEL=INFO
OPS_METRICS_USERNAME=ops
OPS_METRICS_PASSWORD=replace-with-strong-monitoring-password
APP_TIMEZONE=America/Argentina/Buenos_Aires
```

- In `prod`, `OPS_METRICS_USERNAME` and `OPS_METRICS_PASSWORD` must not use default/insecure values.

## Flyway profile locations

- `dev`: `classpath:db/migration/common,classpath:db/migration/dev`
- `qa`: `classpath:db/migration/common,classpath:db/migration/qa`
- `prod`: `classpath:db/migration/common,classpath:db/migration/prod`

## Docker local database

Use `docker-compose.yml` to run local PostgreSQL/PostGIS:

```bash
docker compose --env-file .env.dev up -d postgres
```

Optional compose vars:

```env
POSTGRES_DB=situ_dev
POSTGRES_PORT=5432
```
