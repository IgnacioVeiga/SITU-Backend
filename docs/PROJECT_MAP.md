# Backend Project Map

Quick navigation map for `SITU-Backend`.

## Entry points and config

- `src/main/java/com/backend/situ/Main.java`
- `src/main/resources/application.properties`
- `src/main/resources/application-dev.properties`
- `src/main/resources/application-qa.properties`
- `src/main/resources/application-prod.properties`

## Security and auth

- `src/main/java/com/backend/situ/configs/SecurityConfig.java`
- `src/main/java/com/backend/situ/configs/JwtAuthenticationFilter.java`
- `src/main/java/com/backend/situ/configs/WebConfig.java`
- `src/main/java/com/backend/situ/service/AuthService.java`
- `src/main/java/com/backend/situ/service/JWTService.java`
- `src/main/java/com/backend/situ/service/RefreshTokenCookieService.java`

## Main layers

- `src/main/java/com/backend/situ/controller/**`
- `src/main/java/com/backend/situ/service/**`
- `src/main/java/com/backend/situ/repository/**`
- `src/main/java/com/backend/situ/entity/**`
- `src/main/java/com/backend/situ/model/**`
- `src/main/java/com/backend/situ/exception/**`

## Migrations

- `src/main/resources/db/migration/common`
- `src/main/resources/db/migration/dev`
- `src/main/resources/db/migration/qa`
- `src/main/resources/db/migration/prod`

## Tests

- `src/test/java/com/backend/situ/**`
