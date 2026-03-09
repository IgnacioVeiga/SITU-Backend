# SITU Backend
Spring Boot API for SITU.  
It powers authentication, complaints, alerts, users, transit entities (lines/routes/stops), auditing, and email notifications.

## Stack
- Java 21
- Spring Boot 3.5.x
- PostgreSQL + PostGIS
- Flyway migrations
- JWT auth via HttpOnly cookie

## API prefix
- Official prefix: `/api/v1`
- Legacy prefix `/api/situ` is not used anymore.

## Quick start
1. Copy `.env.example` to your local environment file (`.env.dev`, `.env.qa`, or `.env.prod`).
2. Set `SPRING_PROFILES_ACTIVE` to `dev`, `qa`, or `prod`.
3. Fill required variables (DB, JWT, CORS, mail, encryption key, tracking hash secret).
4. Run:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk bash mvnw spring-boot:run
```

## Local database with Docker
`docker-compose.yml` is database-only (PostgreSQL + PostGIS).

Start DB with your dev env file:

```bash
docker compose --env-file .env.dev up -d postgres
```

Stop DB:

```bash
docker compose down
```

## Starter scripts
You can start backend and/or DB with:

- Linux/macOS: `./scripts/run.sh [dev|qa|prod] [local|docker|auto]`
- PowerShell: `./scripts/run.ps1 [dev|qa|prod] [local|docker|auto]`
- CMD: `scripts/run.bat [dev|qa|prod] [local|docker|auto]`

Examples:

- `./scripts/run.sh dev local` -> start backend app with `.env.dev`.
- `./scripts/run.sh dev docker` -> start only PostgreSQL container using `.env.dev`.
- `./scripts/run.sh dev auto` -> start PostgreSQL container, wait for health, then start backend.

Convenience dev wrappers (no args required):

- Linux/macOS: `./scripts/start-dev.sh`
- PowerShell: `./scripts/start-dev.ps1`
- CMD: `scripts/start-dev.bat`

## Quality checks
Run tests:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk bash mvnw test
```

Build package:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk bash mvnw -DskipTests package
```

## Documentation
- `docs/DEVELOPMENT_SETUP.md`: setup and startup flow.
- `docs/ENVIRONMENTS.md`: profile and env convention quick reference.
- `docs/AUTH_FLOW.md`: auth/session lifecycle summary.
- `docs/MIGRATIONS.md`: standardized Flyway layout and rules.
- `docs/TROUBLESHOOTING.md`: common failures and checks.
- `docs/PROJECT_MAP.md`: source navigation map.
- `docs/environment.md`: detailed environment variables and security notes.
- `docs/MIGRATIONS.md`: detailed Flyway notes.
- `docs/business-rules.md`: complaint/alert business logic.
- `docs/api-v1.md`: current endpoint reference.
- `docs/maps-postgis-leaflet.md`: geospatial contract (PostGIS storage + frontend Leaflet expectations).

## Migration structure
- `src/main/resources/db/migration/common`: shared migrations for every environment.
- `src/main/resources/db/migration/dev`: dev-only data/bootstrap.
- `src/main/resources/db/migration/qa`: qa-only bootstrap.
- `src/main/resources/db/migration/prod`: prod-only bootstrap.

## Geospatial notes
- Route geometry is stored in PostGIS as `GEOMETRY(LineString, 4326)`.
- Stop geometry is stored in PostGIS as `GEOMETRY(Point, 4326)`.
- Write APIs expect GeoJSON coordinates in `[longitude, latitude]` order.
- Read APIs can expose route geometry in database-native text form (EWKB hex) depending on JDBC mapping.

## IntelliJ run configurations
Tracked run configs are available in `.run/`:
- `Backend - Dev` (uses `.env.dev`)
- `Backend - QA` (uses `.env.qa`)
- `Backend - Prod` (uses `.env.prod`)
