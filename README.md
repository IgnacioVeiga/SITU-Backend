# SITU-Backend
Backend de SITU para gestión de reclamos, denuncias, alertas, líneas, recorridos, paradas y usuarios de empresas de transporte.

## Requisitos
- Java 21+
- PostgreSQL 14+ (con PostGIS)

## API
- Prefijo oficial: `/api/v1`
- El prefijo legacy `/api/situ` fue removido.

## Arranque local
1. Copiar `.env.example` a `.env.dev`.
2. Completar variables de base de datos, JWT, CORS y mail.
3. Ejecutar:
```bash
./mvnw spring-boot:run
```

## Calidad
- Ejecutar tests:
```bash
./mvnw test
```
- Compilar empaquetado:
```bash
./mvnw -DskipTests package
```
