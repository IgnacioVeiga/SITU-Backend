package com.backend.situ.configs;

import com.backend.situ.enums.UserRole;
import com.backend.situ.service.AuthService;
import com.backend.situ.service.JWTService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private AuthService authService;

    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        authInterceptor = new AuthInterceptor();
        ReflectionTestUtils.setField(authInterceptor, "jwtService", jwtService);
        ReflectionTestUtils.setField(authInterceptor, "authService", authService);
    }

    @Test
    void shouldAllowPublicLoginEndpointWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        verifyNoInteractions(jwtService, authService);
    }

    @Test
    void shouldAllowNonApiPathWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        verifyNoInteractions(jwtService, authService);
    }

    @Test
    void shouldRejectProtectedEndpointWhenCookieIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/0/10/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("ERRORS.AUTH.INVALID_CREDENTIALS"));
    }

    @Test
    void shouldAllowAdminOnManagementEndpoint() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/0/10/1");
        request.setCookies(new Cookie("authToken", "valid-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.getSubjectFromToken("valid-token")).thenReturn("admin@company.com");
        when(authService.validateAndRenewToken(eq("valid-token"), any())).thenReturn(true);
        when(authService.getRoleFromToken("valid-token")).thenReturn(UserRole.ADMIN);

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        verify(jwtService).getSubjectFromToken("valid-token");
        verify(authService).validateAndRenewToken(eq("valid-token"), any());
        verify(authService).getRoleFromToken("valid-token");
    }

    @Test
    void shouldReturnForbiddenForPassengerOnManagementEndpoint() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/0/10/1");
        request.setCookies(new Cookie("authToken", "valid-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.getSubjectFromToken("valid-token")).thenReturn("passenger@company.com");
        when(authService.validateAndRenewToken(eq("valid-token"), any())).thenReturn(true);
        when(authService.getRoleFromToken("valid-token")).thenReturn(UserRole.PASSENGER);

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("ERRORS.AUTH.INSUFFICIENT_PERMISSIONS"));
    }

    @Test
    void shouldIgnoreLegacyPrefixWhenRoutingNoLongerUsesIt() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/situ/users/0/10/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        verifyNoInteractions(jwtService, authService);
    }

    @Test
    void shouldAllowPublicComplaintTrackingEndpointWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/complaints/tracking/ABC123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
        verifyNoInteractions(jwtService, authService);
    }

    @Test
    void shouldAllowPassengerToCreateAlert() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/alerts");
        request.setCookies(new Cookie("authToken", "valid-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.getSubjectFromToken("valid-token")).thenReturn("passenger@company.com");
        when(authService.validateAndRenewToken(eq("valid-token"), any())).thenReturn(true);
        when(authService.getRoleFromToken("valid-token")).thenReturn(UserRole.PASSENGER);

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
    }

    @Test
    void shouldDenyPassengerWhenUpdatingAlert() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/api/v1/alerts/1");
        request.setCookies(new Cookie("authToken", "valid-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.getSubjectFromToken("valid-token")).thenReturn("passenger@company.com");
        when(authService.validateAndRenewToken(eq("valid-token"), any())).thenReturn(true);
        when(authService.getRoleFromToken("valid-token")).thenReturn(UserRole.PASSENGER);

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("ERRORS.AUTH.INSUFFICIENT_PERMISSIONS"));
    }

    @Test
    void shouldAllowPassengerToReadOwnComplaints() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/complaints/mine/0/10");
        request.setCookies(new Cookie("authToken", "valid-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.getSubjectFromToken("valid-token")).thenReturn("passenger@company.com");
        when(authService.validateAndRenewToken(eq("valid-token"), any())).thenReturn(true);
        when(authService.getRoleFromToken("valid-token")).thenReturn(UserRole.PASSENGER);

        boolean allowed = authInterceptor.preHandle(request, response, new Object());

        assertTrue(allowed);
    }
}
