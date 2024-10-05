# Enviroment variables
This is for reference to create your own “.env” file or assign your own way environment variables.

## Database
Example, if you used the [docker-compose.yml](docker-compose.yml) file:
```properties
DB_URL=jdbc:postgresql://localhost:5432/situ
DB_USERNAME=admin
DB_PASSWORD=admin@1234
```

## Email service
For example, if you use Gmail:
```properties
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=example@gmail.com
MAIL_PASSWORD=example-password
```

## JSON Web Token service
For the secret key in the JWT service use a long base64 string, for example:
```properties
JWT_SECRET=MUwtLVAtKjZJRVwjJT0jZj5lcDxIanY6R1BjPGEyP1V2eSxiXmErbWQhc2NRKHdK
```

## Cookie - Secure
(optional) Default value is “false” in development, set to “true” in production.
```properties
COOKIE_SECURE=true
```

## CORS allowed origins
(Optional) Only if you want to deploy to production: 
```properties
CORS_ALLOWED_ORIGINS=https://your.domain.url
```
´localhost:4200´ was set as default value for development.