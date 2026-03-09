# Backend Auth Flow

Authentication/session flow summary for `SITU-Backend`.

## Current model

- Login uses `email + password`.
- Access token is short-lived JWT (`Bearer`).
- Refresh token is cookie-based (HttpOnly).
- Refresh sessions are persisted server-side as hashed tokens.
- Logout revokes refresh session and clears cookie.

## Auth endpoints

- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `POST /api/v1/auth/signup`
- `GET /api/v1/auth/session`
- `POST /api/v1/auth/password`

## Frontend integration requirements

- Requests use `withCredentials: true` where auth cookie is required.
- Access token is sent via `Authorization: Bearer <token>`.

## Technical references

- `src/main/java/com/backend/situ/service/AuthService.java`
- `src/main/java/com/backend/situ/service/JWTService.java`
- `src/main/java/com/backend/situ/service/RefreshTokenCookieService.java`
- `src/main/java/com/backend/situ/configs/SecurityConfig.java`
- `src/main/java/com/backend/situ/configs/JwtAuthenticationFilter.java`
