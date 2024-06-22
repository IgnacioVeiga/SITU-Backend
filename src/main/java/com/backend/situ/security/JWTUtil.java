package com.backend.situ.security;

import com.backend.situ.entity.UserCredentials;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    // ¡Importante! Necesitas crear un archivo `.env` en la carpeta raíz del proyecto con el token.

    public String generateToken(UserCredentials user) {
        return Jwts.builder()
                .setSubject(user.email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10hs
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // TODO: Añadir métodos para validar y extraer información del token
}
