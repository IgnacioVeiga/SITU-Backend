# Backend environment variables

## Runtime profile
```env
APP_ENV=development
```

## Database
```env
DB_URL=jdbc:postgresql://localhost:5432/situ_dev
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

## JWT and cookie
```env
JWT_SECRET=replace-with-a-strong-secret-min-32-bytes
JWT_EXPIRATION_HOURS=24
JWT_RENEW_THRESHOLD_MINUTES=15

COOKIE_SECURE=false
COOKIE_MAX_AGE_HOURS=24
```

## CORS
```env
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

## Sensitive data encryption
```env
DATA_ENCRYPTION_KEY=replace-with-a-strong-secret
```

`DATA_ENCRYPTION_KEY` is used to encrypt/decrypt complaint contact fields in the database.
Use a strong key in QA/production and rotate it through a controlled process.

## Mail
```env
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-user@example.com
MAIL_PASSWORD=your-app-password
```

Mail is used for signup credentials and complaint status notifications.

## Notes
- `application.properties` only contains non-sensitive defaults.
- Keep real values in local or deployment environment files, not in Git.
