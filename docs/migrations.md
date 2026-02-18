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
