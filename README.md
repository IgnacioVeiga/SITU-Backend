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
1. Copy `.env.example` to your local environment file (`.env.dev` or IDE env profile).
2. Fill required variables (DB, JWT, CORS, mail, encryption key).
3. Run:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk bash mvnw spring-boot:run
```

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
- `docs/environment.md`: environment variables and security notes.
- `docs/business-rules.md`: complaint/alert business logic.
- `docs/api-v1.md`: current endpoint reference.
