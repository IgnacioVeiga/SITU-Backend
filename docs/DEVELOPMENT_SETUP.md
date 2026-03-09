# Backend Development Setup

This guide is the standardized onboarding entry point for `SITU-Backend`.

## 1. Prerequisites

- Java 21
- Maven Wrapper (`./mvnw`)
- PostgreSQL/PostGIS (local container or external instance)
- Optional: IntelliJ IDEA

## 2. Local environment file

Create a local runtime file from the template:

```bash
cp .env.example .env.dev
```

Do not commit `.env.dev`, `.env.qa`, or `.env.prod`.

## 3. Profile selection

Supported runtime profiles:

- `dev`
- `qa`
- `prod`

Profile is selected via `SPRING_PROFILES_ACTIVE`.

## 4. Start options

### Local app process

```bash
./scripts/run.sh dev local
```

### DB container only

```bash
./scripts/run.sh dev docker
```

### DB container + app

```bash
./scripts/run.sh dev auto
```

## 5. Startup checks

On startup, confirm:

- environment validation is coherent,
- Flyway migrations run successfully,
- API starts on expected port (`8080` by default).

## 6. Related docs

- `docs/ENVIRONMENTS.md`
- `docs/AUTH_FLOW.md`
- `docs/MIGRATIONS.md`
- `docs/TROUBLESHOOTING.md`
- `docs/PROJECT_MAP.md`