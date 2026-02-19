# Flyway migration layout

SITU uses a split migration strategy:

- `db/migration/common`: scripts required in every environment.
- `db/migration/dev`: development-only seeds/bootstrap.
- `db/migration/qa`: QA-only seeds/bootstrap.
- `db/migration/prod`: production-only bootstrap scripts.

Resolved locations by profile:

- `dev`: `common + dev`
- `qa`: `common + qa`
- `prod`: `common + prod`

## Rules

1. Put schema and business-critical DDL in `common`.
2. Put sample/test bootstrap data in `dev` only.
3. Keep `prod` scripts deterministic and idempotent when possible.
4. Never move/delete already-applied versioned SQL files in shared branches.

## Current baseline (v2.0.0)

- `common/V1__init_schema.sql` is the single clean bootstrap for a fresh database:
  - creates required extensions (`postgis`),
  - creates all core tables,
  - creates geometry columns (`routes.coordinates`, `stops.location`),
  - creates spatial indexes (GiST) and tenant/lookup indexes.
- `dev/V101__seed_dev_data.sql` provides development fixtures (users, lines, routes, stops, alerts).
- `qa` and `prod` currently do not include sample data.

## Important reset note

- Migration history was unified for a clean install flow.
- Existing databases migrated with old `V0_*` scripts must be recreated before applying this baseline.
