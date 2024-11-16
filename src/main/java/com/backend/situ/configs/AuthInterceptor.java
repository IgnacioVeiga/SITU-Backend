package com.backend.situ.configs;

import com.backend.situ.service.AuthService;
import com.backend.situ.service.JWTService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Ignore OPTIONS
        if (request.getMethod().equalsIgnoreCase("OPTIONS")) {
            return true; // Allow preflight requests to pass without authentication
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        Optional<Cookie> authCookieOpt = Arrays.stream(cookies)
                .filter(cookie -> "authToken".equals(cookie.getName()))
                .findFirst();

        if (authCookieOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String authToken = authCookieOpt.get().getValue();
        try {
            String subject = jwtService.getSubjectFromToken(authToken);
            if (subject == null || subject.isEmpty() || subject.isBlank()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }

            return authService.validateAndRenewToken(authToken, response);
        } catch (ExpiredJwtException | MalformedJwtException e) {
            System.out.println("Exception!\n" + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}
