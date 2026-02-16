package com.backend.situ.configs;

import com.backend.situ.enums.UserRole;
import com.backend.situ.model.ApiResponse;
import com.backend.situ.service.AuthService;
import com.backend.situ.service.JWTService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String AUTH_COOKIE_NAME = "authToken";

    private static final Set<UserRole> STAFF_ROLES = EnumSet.of(
            UserRole.ADMIN,
            UserRole.SUPERVISOR,
            UserRole.EMPLOYEE,
            UserRole.DRIVER
    );

    private static final Set<UserRole> MANAGEMENT_ROLES = EnumSet.of(
            UserRole.ADMIN,
            UserRole.SUPERVISOR
    );

    private static final Set<UserRole> AUTHENTICATED_ROLES = EnumSet.allOf(UserRole.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            if (HttpMethod.OPTIONS.matches(request.getMethod())) {
                return true;
            }

            if (isPublicEndpoint(request)) {
                return true;
            }

            String authToken = extractAuthToken(request);
            if (authToken == null || authToken.isBlank()) {
                return writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "ERRORS.AUTH.INVALID_CREDENTIALS");
            }

            String subject = jwtService.getSubjectFromToken(authToken);
            if (subject == null || subject.isBlank()) {
                return writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "ERRORS.AUTH.INVALID_CREDENTIALS");
            }

            if (!authService.validateAndRenewToken(authToken, response)) {
                return writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "ERRORS.AUTH.INVALID_CREDENTIALS");
            }

            UserRole role = authService.getRoleFromToken(authToken);
            if (role == null) {
                return writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "ERRORS.AUTH.INVALID_CREDENTIALS");
            }

            if (!isRoleAllowed(request, role)) {
                return writeError(response, HttpServletResponse.SC_FORBIDDEN, "ERRORS.AUTH.INSUFFICIENT_PERMISSIONS");
            }

            request.setAttribute("auth.role", role);
            request.setAttribute("auth.subject", subject);
            return true;
        } catch (ExpiredJwtException | MalformedJwtException | IllegalArgumentException e) {
            return writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "ERRORS.AUTH.INVALID_CREDENTIALS");
        }
    }

    private boolean isPublicEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if (!uri.startsWith("/api/situ/auth")) {
            return false;
        }

        return ("/api/situ/auth/login".equals(uri) && HttpMethod.POST.matches(method))
                || ("/api/situ/auth/signup".equals(uri) && HttpMethod.POST.matches(method))
                || ("/api/situ/auth/logout".equals(uri) && HttpMethod.POST.matches(method));
    }

    private String extractAuthToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        Optional<Cookie> authCookieOpt = Arrays.stream(cookies)
                .filter(cookie -> AUTH_COOKIE_NAME.equals(cookie.getName()))
                .findFirst();

        return authCookieOpt.map(Cookie::getValue).orElse(null);
    }

    private boolean isRoleAllowed(HttpServletRequest request, UserRole role) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        if ("/api/situ/auth/session".equals(uri) && HttpMethod.GET.matches(method)) {
            return AUTHENTICATED_ROLES.contains(role);
        }

        if ("/api/situ/auth/password".equals(uri) && HttpMethod.POST.matches(method)) {
            return AUTHENTICATED_ROLES.contains(role);
        }

        if (uri.startsWith("/api/situ/users")) {
            if (HttpMethod.GET.matches(method)) {
                return MANAGEMENT_ROLES.contains(role);
            }
            return MANAGEMENT_ROLES.contains(role);
        }

        if (uri.startsWith("/api/situ/companies")) {
            return MANAGEMENT_ROLES.contains(role);
        }

        if (uri.startsWith("/api/situ/lines")
                || uri.startsWith("/api/situ/routes")
                || uri.startsWith("/api/situ/stops")) {
            if (HttpMethod.GET.matches(method)) {
                return STAFF_ROLES.contains(role);
            }
            return MANAGEMENT_ROLES.contains(role);
        }

        if (uri.startsWith("/api/situ/reports") || uri.startsWith("/api/situ/alerts")) {
            return STAFF_ROLES.contains(role);
        }

        if (uri.startsWith("/api/situ/images")) {
            return STAFF_ROLES.contains(role);
        }

        // Deny by default to avoid exposing new endpoints accidentally.
        return false;
    }

    private boolean writeError(HttpServletResponse response, int status, String messageKey) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.failure(messageKey)));
        return false;
    }
}
