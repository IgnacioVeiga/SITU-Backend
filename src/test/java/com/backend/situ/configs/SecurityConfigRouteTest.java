package com.backend.situ.configs;

import com.backend.situ.service.AuthService;
import com.backend.situ.service.JWTService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigRouteTest.SecurityProbeController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, SecurityConfigRouteTest.SecurityProbeController.class})
class SecurityConfigRouteTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JWTService jwtService;

    @MockBean
    private AuthService authService;

    @Test
    void shouldAllowPublicLoginWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowPublicComplaintTrackingWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/complaints/tracking/ABC123"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnUnauthorizedForProtectedEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users/0/10"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void shouldAllowAdminOnManagementEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/users/0/10").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyPassengerOnManagementEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/users/0/10").with(user("passenger").roles("PASSENGER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("ERRORS.AUTH.INSUFFICIENT_PERMISSIONS"));
    }

    @Test
    void shouldAllowPassengerToCreateAlert() throws Exception {
        mockMvc.perform(post("/api/v1/alerts").with(user("passenger").roles("PASSENGER")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyPassengerWhenUpdatingAlert() throws Exception {
        mockMvc.perform(patch("/api/v1/alerts/1").with(user("passenger").roles("PASSENGER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("ERRORS.AUTH.INSUFFICIENT_PERMISSIONS"));
    }

    @Test
    void shouldAllowPassengerToReadLines() throws Exception {
        mockMvc.perform(get("/api/v1/lines").with(user("passenger").roles("PASSENGER")))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyPassengerWhenCreatingLine() throws Exception {
        mockMvc.perform(post("/api/v1/lines").with(user("passenger").roles("PASSENGER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("ERRORS.AUTH.INSUFFICIENT_PERMISSIONS"));
    }

    @RestController
    @RequestMapping("/api/v1")
    public static class SecurityProbeController {

        @PostMapping("/auth/login")
        public ResponseEntity<Void> login() {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/complaints/tracking/{trackingToken}")
        public ResponseEntity<Void> complaintTracking(@PathVariable String trackingToken) {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/users/{pageIndex}/{pageSize}")
        public ResponseEntity<Void> users(
                @PathVariable int pageIndex,
                @PathVariable int pageSize
        ) {
            return ResponseEntity.ok().build();
        }

        @PostMapping("/alerts")
        public ResponseEntity<Void> createAlert() {
            return ResponseEntity.ok().build();
        }

        @PatchMapping("/alerts/{alertId}")
        public ResponseEntity<Void> updateAlert(@PathVariable long alertId) {
            return ResponseEntity.ok().build();
        }

        @GetMapping("/lines")
        public ResponseEntity<Void> lines() {
            return ResponseEntity.ok().build();
        }

        @PostMapping("/lines")
        public ResponseEntity<Void> createLine() {
            return ResponseEntity.ok().build();
        }
    }
}
