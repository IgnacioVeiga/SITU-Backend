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
    private static final String API_PREFIX = "/api/v1";

    private static final Set<UserRole> STAFF_ROLES = EnumSet.of(
            UserRole.ADMIN,
            UserRole.SUPERVISOR,
            UserRole.EMPLOYEE,
            UserRole.DRIVER
    );

    private static final Set<UserRole> COMPANY_STAFF_ROLES = EnumSet.of(
            UserRole.ADMIN,
            UserRole.SUPERVISOR,
            UserRole.EMPLOYEE
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

            String normalizedPath = normalizeApiPath(request.getRequestURI());
            if (normalizedPath == null) {
                return true;
            }

            if (isPublicEndpoint(normalizedPath, request.getMethod())) {
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

            if (!isRoleAllowed(normalizedPath, request.getMethod(), role)) {
                return writeError(response, HttpServletResponse.SC_FORBIDDEN, "ERRORS.AUTH.INSUFFICIENT_PERMISSIONS");
            }

            request.setAttribute("auth.role", role);
            request.setAttribute("auth.subject", subject);
            return true;
        } catch (ExpiredJwtException | MalformedJwtException | IllegalArgumentException e) {
            return writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "ERRORS.AUTH.INVALID_CREDENTIALS");
        }
    }

    private boolean isPublicEndpoint(String path, String method) {
        return ("/auth/login".equals(path) && HttpMethod.POST.matches(method))
                || ("/auth/signup".equals(path) && HttpMethod.POST.matches(method))
                || ("/auth/logout".equals(path) && HttpMethod.POST.matches(method))
                || (path.startsWith("/complaints/tracking/") && HttpMethod.GET.matches(method))
                || (path.startsWith("/reports/tracking/") && HttpMethod.GET.matches(method));
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

    private boolean isRoleAllowed(String path, String method, UserRole role) {
        if ("/auth/session".equals(path) && HttpMethod.GET.matches(method)) {
            return AUTHENTICATED_ROLES.contains(role);
        }

        if ("/auth/password".equals(path) && HttpMethod.POST.matches(method)) {
            return AUTHENTICATED_ROLES.contains(role);
        }

        if (path.startsWith("/users")) {
            return MANAGEMENT_ROLES.contains(role);
        }

        if (path.startsWith("/companies")) {
            return MANAGEMENT_ROLES.contains(role);
        }

        if (path.startsWith("/lines")
                || path.startsWith("/routes")
                || path.startsWith("/stops")) {
            if (HttpMethod.GET.matches(method)) {
                return AUTHENTICATED_ROLES.contains(role);
            }
            return MANAGEMENT_ROLES.contains(role);
        }

        if (path.startsWith("/complaints") || path.startsWith("/reports")) {
            if (HttpMethod.POST.matches(method)) {
                return AUTHENTICATED_ROLES.contains(role);
            }

            if (HttpMethod.GET.matches(method) || HttpMethod.PATCH.matches(method)) {
                return COMPANY_STAFF_ROLES.contains(role);
            }

            return false;
        }

        if (path.startsWith("/alerts")) {
            if (HttpMethod.GET.matches(method) || HttpMethod.POST.matches(method)) {
                return AUTHENTICATED_ROLES.contains(role);
            }

            if (HttpMethod.PATCH.matches(method) || HttpMethod.PUT.matches(method) || HttpMethod.DELETE.matches(method)) {
                return COMPANY_STAFF_ROLES.contains(role);
            }

            return false;
        }

        if (path.startsWith("/images")) {
            return STAFF_ROLES.contains(role);
        }

        // Deny by default to avoid exposing new endpoints accidentally.
        return false;
    }

    private String normalizeApiPath(String uri) {
        if (!uri.startsWith(API_PREFIX)) {
            return null;
        }

        String suffix = uri.substring(API_PREFIX.length());
        if (suffix.isBlank()) {
            return "/";
        }

        return suffix.startsWith("/") ? suffix : "/" + suffix;
    }

    private boolean writeError(HttpServletResponse response, int status, String messageKey) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.failure(messageKey)));
        return false;
    }
}
