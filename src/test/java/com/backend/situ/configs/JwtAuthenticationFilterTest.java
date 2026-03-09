package com.backend.situ.configs;

import com.backend.situ.enums.UserRole;
import com.backend.situ.service.AuthService;
import com.backend.situ.service.JWTService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSkipWhenAuthorizationHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/0/10");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNotNull(filterChain.getRequest());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtService, authService);
    }

    @Test
    void shouldSetAuthErrorWhenTokenIsExpired() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/0/10");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer expired-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(jwtService.isTokenExpired("expired-token")).thenReturn(true);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNotNull(filterChain.getRequest());
        assertEquals("ERRORS.AUTH.INVALID_CREDENTIALS", request.getAttribute("auth.error"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService).isTokenExpired("expired-token");
        verifyNoInteractions(authService);
    }

    @Test
    void shouldAuthenticateAndSetRequestAttributesForValidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/0/10");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(jwtService.isTokenExpired("valid-token")).thenReturn(false);
        when(jwtService.getSubjectFromToken("valid-token")).thenReturn("admin@company.com");
        when(authService.getRoleFromToken("valid-token")).thenReturn(UserRole.ADMIN);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNotNull(filterChain.getRequest());
        assertEquals("admin@company.com", request.getAttribute("auth.subject"));
        assertEquals(UserRole.ADMIN, request.getAttribute("auth.role"));
        assertNull(request.getAttribute("auth.error"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("admin@company.com", authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
    }

    @Test
    void shouldSetAuthErrorWhenTokenParsingThrows() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/0/10");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer malformed-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        when(jwtService.isTokenExpired("malformed-token")).thenThrow(new RuntimeException("boom"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNotNull(filterChain.getRequest());
        assertEquals("ERRORS.AUTH.INVALID_CREDENTIALS", request.getAttribute("auth.error"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
