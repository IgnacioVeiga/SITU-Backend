# Backend Troubleshooting

Common runtime issues and quick checks for `SITU-Backend`.

## 1. Startup fails before controllers are ready

Check first root cause in logs. Most frequent causes:

- invalid/missing env vars,
- DB connectivity errors,
- Flyway migration failure.

## 2. Flyway migration errors

Verify:

- active profile,
- matching migration folders,
- DB user permissions,
- clean baseline usage for fresh vs legacy databases.

## 3. Auth requests return 401

Verify:

- valid bearer token for protected endpoints,
- refresh cookie present for refresh/logout flows,
- token not expired,
- frontend sending credentials where required.

## 4. CORS/cookie integration issues

Verify:

- `CORS_ALLOWED_ORIGINS` includes exact frontend origin,
- refresh cookie settings match deployment (`SameSite`/`Secure`),
- frontend `apiBaseUrl` matches current environment.

## 5. Geospatial/map payload failures

Verify:

- route payload uses GeoJSON `LineString` (`[lon, lat]`),
- stop payload uses GeoJSON `Point` (`[lon, lat]`),
- route has at least 2 points.

## References

- `docs/environment.md`
- `docs/api-v1.md`
- `docs/maps-postgis-leaflet.md`