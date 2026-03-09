# Backend Migrations

Flyway migration strategy for `SITU-Backend`.

## Folder layout

- `src/main/resources/db/migration/common`
- `src/main/resources/db/migration/dev`
- `src/main/resources/db/migration/qa`
- `src/main/resources/db/migration/prod`

## Profile mapping

- `dev` = `common + dev`
- `qa` = `common + qa`
- `prod` = `common + prod`

## Rules

1. Keep schema evolution in Flyway only.
2. Keep shared DDL and business-critical schema in `common`.
3. Keep environment-specific bootstrap/seed data in env-specific folders.
4. Never move or rewrite already applied versioned scripts in shared environments.

## Current baseline (v2.0.0)

- `common/V1__init_schema.sql`
  - creates required extensions (`postgis`),
  - creates all core tables,
  - creates geometry columns (`routes.coordinates`, `stops.location`),
  - creates spatial indexes (GiST) and tenant/lookup indexes.
- `common/V2__add_refresh_sessions.sql`
  - adds refresh session persistence for cookie/JWT auth flow.
- `dev/V101__seed_dev_data.sql`
  - provides development fixtures (users, lines, routes, stops, alerts).

`qa` and `prod` currently do not include sample data migrations.

## Important reset note

- Migration history was unified for a clean install flow.
- Existing databases migrated with legacy pre-2.0 scripts must be recreated before applying this baseline.

## Related docs

- `docs/DEVELOPMENT_SETUP.md`
- `docs/ENVIRONMENTS.md`
- `docs/maps-postgis-leaflet.md`
