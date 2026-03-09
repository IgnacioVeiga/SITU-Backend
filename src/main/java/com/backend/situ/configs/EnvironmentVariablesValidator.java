package com.backend.situ.configs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.env.validation.enabled", havingValue = "true", matchIfMissing = true)
public class EnvironmentVariablesValidator implements ApplicationRunner {

    private static final Set<String> VALID_PROFILES = Set.of("dev", "qa", "prod");
    private static final Set<String> INSECURE_MONITORING_DEFAULTS = Set.of(
            "ops",
            "admin",
            "password",
            "changeme",
            "change-me",
            "123456"
    );

    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        Set<String> activeProfiles = resolveActiveProfiles();
        if (activeProfiles.stream().noneMatch(VALID_PROFILES::contains)) {
            return;
        }

        List<String> errors = new ArrayList<>();

        String dbUrl = requireEnv("DB_URL", errors, "Example: jdbc:postgresql://host:5432/situ?sslmode=require");
        requireEnv("DB_USERNAME", errors, "Example: postgres");
        requireEnv("DB_PASSWORD", errors, "Example: strong-password");
        String jwtSecret = requireEnv("JWT_SECRET", errors, "At least 32 characters");
        String corsAllowedOrigins = requireEnv("CORS_ALLOWED_ORIGINS", errors, "Comma-separated http(s) origins");
        requireEnv("DATA_ENCRYPTION_KEY", errors, "Use a long random key");
        requireEnv("TRACKING_TOKEN_HASH_SECRET", errors, "Use a long random key");

        validateDbUrl(dbUrl, errors);
        validateJwtSecret(jwtSecret, errors);
        validateCors(corsAllowedOrigins, errors);
        validateRefreshExpirations(errors);
        validateRefreshCookie(errors);

        if (activeProfiles.contains("prod")) {
            validateProdConstraints(errors);
        }

        if (!errors.isEmpty()) {
            String message = buildValidationMessage(activeProfiles, errors);
            log.error(message);
            throw new IllegalStateException(message);
        }

        log.info("Environment validation passed for profiles {}", activeProfiles);
    }

    private Set<String> resolveActiveProfiles() {
        Set<String> profiles = new LinkedHashSet<>(Arrays.asList(environment.getActiveProfiles()));
        if (profiles.isEmpty()) {
            profiles.addAll(Arrays.asList(environment.getDefaultProfiles()));
        }
        if (profiles.isEmpty()) {
            profiles.add("dev");
        }
        return profiles;
    }

    private String requireEnv(String variableName, List<String> errors, String hint) {
        String value = environment.getProperty(variableName);
        if (!StringUtils.hasText(value)) {
            errors.add("Missing environment variable '" + variableName + "'. " + hint);
            return "";
        }
        return value.trim();
    }

    private void validateDbUrl(String dbUrl, List<String> errors) {
        if (!StringUtils.hasText(dbUrl)) {
            return;
        }
        if (!dbUrl.startsWith("jdbc:postgresql://")) {
            errors.add("DB_URL must start with 'jdbc:postgresql://'.");
        }
    }

    private void validateJwtSecret(String jwtSecret, List<String> errors) {
        if (StringUtils.hasText(jwtSecret) && jwtSecret.length() < 32) {
            errors.add("JWT_SECRET must have at least 32 characters.");
        }
    }

    private void validateCors(String corsAllowedOrigins, List<String> errors) {
        if (!StringUtils.hasText(corsAllowedOrigins)) {
            return;
        }

        List<String> origins = Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();

        if (origins.isEmpty()) {
            errors.add("CORS_ALLOWED_ORIGINS must contain at least one origin.");
            return;
        }

        if (origins.contains("*")) {
            errors.add("CORS_ALLOWED_ORIGINS must not contain wildcard '*'.");
        }

        for (String origin : origins) {
            String normalized = origin.toLowerCase(Locale.ROOT);
            if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
                errors.add("CORS_ALLOWED_ORIGINS contains invalid origin: " + origin);
            }
        }
    }

    private void validateRefreshExpirations(List<String> errors) {
        Long refresh = parseOptionalPositiveLong(
                "AUTH_REFRESH_EXPIRATION_SECONDS",
                "AUTH_REFRESH_EXPIRATION_SECONDS must be a positive integer.",
                errors
        );
        Long remember = parseOptionalPositiveLong(
                "AUTH_REFRESH_REMEMBER_EXPIRATION_SECONDS",
                "AUTH_REFRESH_REMEMBER_EXPIRATION_SECONDS must be a positive integer.",
                errors
        );

        if (refresh != null && remember != null && remember < refresh) {
            errors.add("AUTH_REFRESH_REMEMBER_EXPIRATION_SECONDS must be >= AUTH_REFRESH_EXPIRATION_SECONDS.");
        }
    }

    private void validateRefreshCookie(List<String> errors) {
        String sameSite = environment.getProperty("AUTH_REFRESH_COOKIE_SAME_SITE", "Lax")
                .trim()
                .toLowerCase(Locale.ROOT);
        if (!Set.of("lax", "strict", "none").contains(sameSite)) {
            errors.add("AUTH_REFRESH_COOKIE_SAME_SITE must be one of: Lax, Strict, None.");
            return;
        }

        boolean secureCookie = Boolean.parseBoolean(
                environment.getProperty("AUTH_REFRESH_COOKIE_SECURE", "false")
        );
        if ("none".equals(sameSite) && !secureCookie) {
            errors.add("AUTH_REFRESH_COOKIE_SECURE must be true when AUTH_REFRESH_COOKIE_SAME_SITE=None.");
        }
    }

    private void validateProdConstraints(List<String> errors) {
        boolean secureCookie = Boolean.parseBoolean(
                environment.getProperty("AUTH_REFRESH_COOKIE_SECURE", "false")
        );
        if (!secureCookie) {
            errors.add("AUTH_REFRESH_COOKIE_SECURE must be true in prod.");
        }

        String corsAllowedOrigins = environment.getProperty("CORS_ALLOWED_ORIGINS", "");
        boolean hasHttpOrigin = Arrays.stream(corsAllowedOrigins.split(","))
                .map(String::trim)
                .anyMatch(origin -> origin.toLowerCase(Locale.ROOT).startsWith("http://"));
        if (hasHttpOrigin) {
            log.warn("CORS_ALLOWED_ORIGINS includes http:// origins in prod profile.");
        }

        validateOpsMonitoringCredentials(errors);
    }

    private void validateOpsMonitoringCredentials(List<String> errors) {
        String username = environment.getProperty("OPS_METRICS_USERNAME", "ops").trim();
        String password = environment.getProperty("OPS_METRICS_PASSWORD", "ops").trim();

        if (!StringUtils.hasText(username)) {
            errors.add("OPS_METRICS_USERNAME must not be blank in prod.");
        }
        if (!StringUtils.hasText(password)) {
            errors.add("OPS_METRICS_PASSWORD must not be blank in prod.");
        }

        if (INSECURE_MONITORING_DEFAULTS.contains(username.toLowerCase(Locale.ROOT))
                || INSECURE_MONITORING_DEFAULTS.contains(password.toLowerCase(Locale.ROOT))) {
            errors.add("OPS_METRICS_USERNAME/OPS_METRICS_PASSWORD must not use insecure default values in prod.");
        }
    }

    private Long parseOptionalPositiveLong(String key, String invalidMessage, List<String> errors) {
        String value = environment.getProperty(key);
        if (!StringUtils.hasText(value)) {
            return null;
        }

        try {
            long parsed = Long.parseLong(value.trim());
            if (parsed <= 0) {
                errors.add(invalidMessage);
                return null;
            }
            return parsed;
        } catch (NumberFormatException ex) {
            errors.add(invalidMessage);
            return null;
        }
    }

    private String buildValidationMessage(Set<String> profiles, List<String> errors) {
        StringBuilder builder = new StringBuilder();
        builder.append("Environment validation failed for profiles ").append(profiles).append(".\n")
                .append("The application cannot start until these issues are fixed:\n");
        errors.forEach(error -> builder.append(" - ").append(error).append('\n'));
        builder.append("Tip: update the corresponding .env file (for example '.env.dev').");
        return builder.toString();
    }
}
