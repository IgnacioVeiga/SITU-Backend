# Backend environment variables

## Profile selection
```env
SPRING_PROFILES_ACTIVE=dev
APP_ENV=dev
```

- Supported profiles: `dev`, `qa`, `prod`.
- `APP_ENV` is optional and only mirrors profile information inside app-level properties.
- If `SPRING_PROFILES_ACTIVE` is not set, backend defaults to `dev`.

## Database
```env
DB_URL=jdbc:postgresql://localhost:5432/situ_dev
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

## JWT and cookie
```env
JWT_SECRET=replace-with-a-strong-secret-min-32-bytes
JWT_EXPIRATION_HOURS=24
JWT_RENEW_THRESHOLD_MINUTES=15

COOKIE_SECURE=false
COOKIE_MAX_AGE_HOURS=24
```

## CORS
```env
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

## Sensitive data encryption
```env
DATA_ENCRYPTION_KEY=replace-with-a-strong-secret
```

`DATA_ENCRYPTION_KEY` is used to encrypt/decrypt complaint contact fields in the database.
Use a strong key in QA/production and rotate it through a controlled process.

## Tracking token hashing
```env
TRACKING_TOKEN_HASH_SECRET=replace-with-a-different-strong-secret
```

`TRACKING_TOKEN_HASH_SECRET` is used for HMAC-SHA256 hashing of complaint tracking tokens.
The plain tracking token is never stored in DB.

## Mail
```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-user@example.com
MAIL_PASSWORD=your-app-password
```

Mail is used for signup credentials and complaint status notifications.

## Logging and server timezone
```env
LOG_SECURITY_LEVEL=INFO
APP_TIMEZONE=America/Argentina/Buenos_Aires
```

## Flyway locations by profile
- `dev`: `classpath:db/migration/common,classpath:db/migration/dev`
- `qa`: `classpath:db/migration/common,classpath:db/migration/qa`
- `prod`: `classpath:db/migration/common,classpath:db/migration/prod`

## Notes
- `application.properties` contains non-sensitive defaults and shared configuration.
- Use `application-dev.properties`, `application-qa.properties`, and `application-prod.properties` for profile-specific overrides.
- Keep real values in local/deployment environment files, never in Git.

## Docker local database

Use `docker-compose.yml` to run only PostgreSQL/PostGIS:

```bash
docker compose --env-file .env.dev up -d postgres
```

Useful optional vars for compose:

```env
POSTGRES_DB=situ_dev
POSTGRES_PORT=5432
```

## IntelliJ setup (recommended for dev)

Create a Docker Compose run configuration called `DB - Dev`:

1. Compose file: `docker-compose.yml`
2. Service: `postgres`
3. Command: `up`
4. Options: `-d`
5. Environment file: `.env.dev`

If your IntelliJ version does not support an environment file in Docker Compose config, set these variables manually in the run configuration:

```env
DB_USERNAME=user_admin
DB_PASSWORD=admin@123
POSTGRES_DB=situ_dev
POSTGRES_PORT=5432
APP_TIMEZONE=America/Argentina/Buenos_Aires
```

Then run backend using `Backend - Dev` (`.run/Backend - Dev.run.xml`), which loads `.env.dev`.

## Automatic startup scripts

Use one command to start DB + backend in dev:

- Linux/macOS: `./scripts/start-dev.sh`
- PowerShell: `./scripts/start-dev.ps1`
- CMD: `scripts/start-dev.bat`

These wrappers call `run.* dev auto`, which:

1. starts PostgreSQL with `.env.dev`
2. waits until DB health is `healthy`
3. starts Spring Boot in dev profile
