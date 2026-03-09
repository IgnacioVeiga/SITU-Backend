package com.backend.situ.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] AUTHENTICATED_ROLES = {
            "ADMIN",
            "SUPERVISOR",
            "EMPLOYEE",
            "DRIVER",
            "PASSENGER",
            "REGULAR"
    };

    private static final String[] MANAGEMENT_ROLES = {
            "ADMIN",
            "SUPERVISOR"
    };

    private static final String[] COMPANY_STAFF_ROLES = {
            "ADMIN",
            "SUPERVISOR",
            "EMPLOYEE"
    };

    private static final String[] STAFF_ROLES = {
            "ADMIN",
            "SUPERVISOR",
            "EMPLOYEE",
            "DRIVER"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    @Value("${app.env:dev}")
    private String appEnv;

    @Value("${monitoring.ops.username:ops}")
    private String opsUsername;

    @Value("${monitoring.ops.password:ops}")
    private String opsPassword;

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/actuator/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/actuator/health", "/actuator/info").permitAll();
                    if (isProduction()) {
                        auth.requestMatchers("/actuator/prometheus").hasRole("OPS");
                    } else {
                        auth.requestMatchers("/actuator/prometheus").permitAll();
                    }
                    auth.anyRequest().denyAll();
                })
                .httpBasic(Customizer.withDefaults())
                .userDetailsService(opsUserDetailsService())
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain applicationSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/signup").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/complaints/tracking/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/session").hasAnyRole(AUTHENTICATED_ROLES)
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/password").hasAnyRole(AUTHENTICATED_ROLES)

                        .requestMatchers("/api/v1/users/**").hasAnyRole(MANAGEMENT_ROLES)
                        .requestMatchers("/api/v1/companies/**").hasAnyRole(MANAGEMENT_ROLES)

                        .requestMatchers(HttpMethod.GET, "/api/v1/lines/**").hasAnyRole(AUTHENTICATED_ROLES)
                        .requestMatchers(HttpMethod.GET, "/api/v1/routes/**").hasAnyRole(AUTHENTICATED_ROLES)
                        .requestMatchers(HttpMethod.GET, "/api/v1/stops/**").hasAnyRole(AUTHENTICATED_ROLES)
                        .requestMatchers("/api/v1/lines/**").hasAnyRole(MANAGEMENT_ROLES)
                        .requestMatchers("/api/v1/routes/**").hasAnyRole(MANAGEMENT_ROLES)
                        .requestMatchers("/api/v1/stops/**").hasAnyRole(MANAGEMENT_ROLES)

                        .requestMatchers(HttpMethod.GET, "/api/v1/complaints/mine/**").hasAnyRole(AUTHENTICATED_ROLES)
                        .requestMatchers(HttpMethod.POST, "/api/v1/complaints").hasAnyRole(AUTHENTICATED_ROLES)
                        .requestMatchers(HttpMethod.GET, "/api/v1/complaints/**").hasAnyRole(COMPANY_STAFF_ROLES)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/complaints/**").hasAnyRole(COMPANY_STAFF_ROLES)

                        .requestMatchers(HttpMethod.GET, "/api/v1/alerts/**").hasAnyRole(AUTHENTICATED_ROLES)
                        .requestMatchers(HttpMethod.POST, "/api/v1/alerts/**").hasAnyRole(AUTHENTICATED_ROLES)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/alerts/**").hasAnyRole(COMPANY_STAFF_ROLES)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/alerts/**").hasAnyRole(COMPANY_STAFF_ROLES)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/alerts/**").hasAnyRole(COMPANY_STAFF_ROLES)

                        .requestMatchers("/api/v1/images/**").hasAnyRole(STAFF_ROLES)

                        .requestMatchers("/api/v1/**").denyAll()
                        .anyRequest().denyAll()
                )
                .exceptionHandling(configurer -> configurer
                        .authenticationEntryPoint((request, response, authException) -> {
                            String message = (String) request.getAttribute("auth.error");
                            if (message == null || message.isBlank()) {
                                message = "Authentication required";
                            }
                            writeErrorResponse(response, HttpStatus.UNAUTHORIZED, message);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeErrorResponse(response, HttpStatus.FORBIDDEN, "ERRORS.AUTH.INSUFFICIENT_PERMISSIONS")
                        )
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService opsUserDetailsService() {
        return new InMemoryUserDetailsManager(
                User.withUsername(opsUsername)
                        .password(passwordEncoder().encode(opsPassword))
                        .roles("OPS")
                        .build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter filter
    ) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    private boolean isProduction() {
        return "prod".equalsIgnoreCase(appEnv);
    }

    private void writeErrorResponse(
            jakarta.servlet.http.HttpServletResponse response,
            HttpStatus status,
            String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC));
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
