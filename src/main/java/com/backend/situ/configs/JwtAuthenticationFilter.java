package com.backend.situ.configs;

import com.backend.situ.enums.UserRole;
import com.backend.situ.service.AuthService;
import com.backend.situ.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();
        try {
            if (!token.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.isTokenExpired(token)) {
                    request.setAttribute("auth.error", "ERRORS.AUTH.INVALID_CREDENTIALS");
                    filterChain.doFilter(request, response);
                    return;
                }

                String subject = jwtService.getSubjectFromToken(token);
                UserRole role = authService.getRoleFromToken(token);
                if (subject == null || subject.isBlank() || role == null) {
                    request.setAttribute("auth.error", "ERRORS.AUTH.INVALID_CREDENTIALS");
                    filterChain.doFilter(request, response);
                    return;
                }

                request.setAttribute("auth.subject", subject);
                request.setAttribute("auth.role", role);

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        subject,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                );
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        } catch (Exception ex) {
            request.setAttribute("auth.error", "ERRORS.AUTH.INVALID_CREDENTIALS");
        }

        filterChain.doFilter(request, response);
    }
}
