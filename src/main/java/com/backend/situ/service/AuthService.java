package com.backend.situ.service;

import com.backend.situ.entity.Company;
import com.backend.situ.entity.RefreshSession;
import com.backend.situ.entity.User;
import com.backend.situ.entity.UserCredentials;
import com.backend.situ.enums.AuditAction;
import com.backend.situ.enums.UserRole;
import com.backend.situ.event.AuditEvent;
import com.backend.situ.exception.UnauthorizedException;
import com.backend.situ.model.AuthTokenResponse;
import com.backend.situ.model.ChangePasswordDTO;
import com.backend.situ.model.LoginDTO;
import com.backend.situ.model.SessionDTO;
import com.backend.situ.model.SignupDTO;
import com.backend.situ.repository.AuthRepository;
import com.backend.situ.repository.RefreshSessionRepository;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

import org.springframework.context.ApplicationEventPublisher;

@Service
public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private static final ZoneOffset STORAGE_ZONE = ZoneOffset.UTC;
    private static final String PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%^&*";
    private static final int GENERATED_PASSWORD_LENGTH = 16;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${security.auth.refresh.expiration-seconds:43200}")
    private long refreshExpirationSeconds;

    @Value("${security.auth.refresh.remember-expiration-seconds:2592000}")
    private long refreshRememberExpirationSeconds;

    private final ApplicationEventPublisher eventPublisher;
    private final AuthRepository authRepository;
    private final RefreshSessionRepository refreshSessionRepository;
    private final EmailService emailService;
    private final JWTService jwtService;
    private final RefreshTokenCookieService refreshTokenCookieService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public AuthService(
            ApplicationEventPublisher eventPublisher,
            AuthRepository authRepository,
            RefreshSessionRepository refreshSessionRepository,
            EmailService emailService,
            JWTService jwtService,
            RefreshTokenCookieService refreshTokenCookieService
    ) {
        this.eventPublisher = eventPublisher;
        this.authRepository = authRepository;
        this.refreshSessionRepository = refreshSessionRepository;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.refreshTokenCookieService = refreshTokenCookieService;
    }

    @PostConstruct
    void validateAuthConfiguration() {
        if (refreshExpirationSeconds <= 0) {
            throw new IllegalStateException("security.auth.refresh.expiration-seconds must be positive");
        }
        if (refreshRememberExpirationSeconds <= 0) {
            throw new IllegalStateException("security.auth.refresh.remember-expiration-seconds must be positive");
        }
        if (refreshRememberExpirationSeconds < refreshExpirationSeconds) {
            throw new IllegalStateException(
                    "security.auth.refresh.remember-expiration-seconds must be >= security.auth.refresh.expiration-seconds"
            );
        }
    }

    public AuthTokenResponse doLogin(LoginDTO form, HttpServletResponse response) {
        if (form == null) {
            return null;
        }

        String normalizedEmail = normalizeEmail(form.email());
        UserCredentials userCred = this.authRepository.findByEmail(normalizedEmail).orElse(null);

        if (userCred == null || !passwordEncoder.matches(form.password(), userCred.getPassword())) {
            return null;
        }

        IssuedRefreshToken issuedRefreshToken = createRefreshSession(userCred.getUser().getId(), false);
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshTokenCookieService.buildRefreshCookieHeader(
                        issuedRefreshToken.rawToken(),
                        issuedRefreshToken.maxAgeSeconds()
                )
        );

        String details = "New login by user: " + userCred.getUsername();
        AuditEvent auditEvent = new AuditEvent(this, AuditAction.LOGIN, userCred.getUsername(), details);
        eventPublisher.publishEvent(auditEvent);

        return buildAuthTokenResponse(userCred);
    }

    public AuthTokenResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String rawRefreshToken = refreshTokenCookieService.extractRefreshToken(request)
                .orElseThrow(() -> invalidRefreshToken(response));
        String tokenHash = hashRefreshToken(rawRefreshToken);

        RefreshSession currentSession = refreshSessionRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> invalidRefreshToken(response));

        LocalDateTime now = nowUtc();
        if (currentSession.getRevokedAt() != null || !currentSession.getExpiresAt().isAfter(now)) {
            markSessionRevokedIfNeeded(currentSession, now);
            throw invalidRefreshToken(response);
        }

        UserCredentials userCred = authRepository.findByUserId(currentSession.getUserId())
                .orElseThrow(() -> invalidRefreshToken(response));

        IssuedRefreshToken issuedRefreshToken = rotateRefreshSession(currentSession, now);
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshTokenCookieService.buildRefreshCookieHeader(
                        issuedRefreshToken.rawToken(),
                        issuedRefreshToken.maxAgeSeconds()
                )
        );

        return buildAuthTokenResponse(userCred);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        refreshTokenCookieService.extractRefreshToken(request)
                .map(this::hashRefreshToken)
                .flatMap(refreshSessionRepository::findByTokenHash)
                .ifPresent(session -> {
                    if (session.getRevokedAt() == null) {
                        LocalDateTime now = nowUtc();
                        session.setRevokedAt(now);
                        session.setLastUsedAt(now);
                        refreshSessionRepository.save(session);
                    }
                });

        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieService.buildClearCookieHeader());
    }

    public String generateRandomPassword() {
        StringBuilder password = new StringBuilder(GENERATED_PASSWORD_LENGTH);
        for (int i = 0; i < GENERATED_PASSWORD_LENGTH; i++) {
            int index = secureRandom.nextInt(PASSWORD_ALPHABET.length());
            password.append(PASSWORD_ALPHABET.charAt(index));
        }

        return password.toString();
    }

    public void signup(SignupDTO form, User user) {
        String randomPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(randomPassword);

        UserCredentials newUser = new UserCredentials(user, normalizeEmail(form.email()), encodedPassword);
        this.authRepository.save(newUser);

        try {
            this.emailService.sendRegistrationEmail(form.email(), randomPassword);
        } catch (MessagingException e) {
            LOGGER.error("Could not deliver signup email to {}", form.email(), e);
            throw new IllegalStateException("ERRORS.AUTH.EMAIL_DELIVERY_FAILED");
        }

        String details = "Signup by user: " + newUser.getUsername();
        AuditEvent auditEvent = new AuditEvent(this, AuditAction.SIGNUP, newUser.getUsername(), details);
        eventPublisher.publishEvent(auditEvent);
    }

    public boolean existsEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return this.authRepository.findByEmail(normalizeEmail(email)).isPresent();
    }

    public int changePassword(String email, ChangePasswordDTO form) {
        String normalizedEmail = normalizeEmail(email);
        UserCredentials user = this.authRepository.findByEmail(normalizedEmail).orElse(null);
        if (user == null) {
            return HttpServletResponse.SC_NOT_FOUND;
        }

        if (form.currentPassword() == null || form.currentPassword().isBlank()) {
            return HttpServletResponse.SC_BAD_REQUEST;
        }

        if (!passwordEncoder.matches(form.currentPassword(), user.getPassword())) {
            return HttpServletResponse.SC_BAD_REQUEST;
        }

        String newEncodedPassword = passwordEncoder.encode(form.newPassword());
        user.setEncodedPassword(newEncodedPassword);
        this.authRepository.save(user);

        String details = "Password changed by user: " + user.getUsername();
        AuditEvent auditEvent = new AuditEvent(this, AuditAction.CHANGE_PASSWORD, user.getUsername(), details);
        eventPublisher.publishEvent(auditEvent);

        return HttpServletResponse.SC_OK;
    }

    public UserRole getRoleFromToken(String accessToken) {
        String email = jwtService.getSubjectFromToken(accessToken);
        if (email == null || email.isBlank()) {
            return null;
        }

        UserCredentials userCredentials = this.authRepository.findByEmail(email).orElse(null);
        if (userCredentials == null || userCredentials.getUser() == null) {
            return null;
        }

        return userCredentials.getUser().getRole();
    }

    public SessionDTO getSessionData(String email) {
        String normalizedEmail = normalizeEmail(email);
        UserCredentials userCredentials = this.authRepository.findByEmail(normalizedEmail).orElse(null);
        if (userCredentials == null) {
            return null;
        }

        return getSessionData(userCredentials);
    }

    public SessionDTO getSessionData(UserCredentials userCredentials) {
        User user = userCredentials.getUser();
        Company company = user.getCompany();

        return new SessionDTO(
                user.getId(),
                company.getId(),
                company.getLogo_filename(),
                userCredentials.getUsername(),
                user.getFirstName() + " " + user.getLastName(),
                user.getRole()
        );
    }

    private AuthTokenResponse buildAuthTokenResponse(UserCredentials userCredentials) {
        String accessToken = jwtService.getToken(userCredentials);
        OffsetDateTime expiresAt = OffsetDateTime.ofInstant(jwtService.computeExpirationInstant(), ZoneOffset.UTC);
        return new AuthTokenResponse(
                "Bearer",
                accessToken,
                expiresAt,
                getSessionData(userCredentials)
        );
    }

    private IssuedRefreshToken createRefreshSession(Long userId, boolean rememberMe) {
        long ttlSeconds = rememberMe ? refreshRememberExpirationSeconds : refreshExpirationSeconds;
        LocalDateTime now = nowUtc();
        LocalDateTime expiresAt = now.plusSeconds(ttlSeconds);
        return saveRefreshSession(userId, rememberMe, now, expiresAt, ttlSeconds);
    }

    private IssuedRefreshToken rotateRefreshSession(RefreshSession currentSession, LocalDateTime now) {
        markSessionRevokedIfNeeded(currentSession, now);

        long maxAgeSeconds = ChronoUnit.SECONDS.between(now, currentSession.getExpiresAt());
        if (maxAgeSeconds <= 0) {
            throw new UnauthorizedException("ERRORS.AUTH.INVALID_CREDENTIALS");
        }

        return saveRefreshSession(
                currentSession.getUserId(),
                currentSession.isRememberMe(),
                now,
                currentSession.getExpiresAt(),
                maxAgeSeconds
        );
    }

    private IssuedRefreshToken saveRefreshSession(
            Long userId,
            boolean rememberMe,
            LocalDateTime now,
            LocalDateTime expiresAt,
            long maxAgeSeconds
    ) {
        String rawToken = generateRefreshToken();
        RefreshSession newSession = RefreshSession.builder()
                .userId(userId)
                .tokenHash(hashRefreshToken(rawToken))
                .rememberMe(rememberMe)
                .issuedAt(now)
                .lastUsedAt(now)
                .expiresAt(expiresAt)
                .build();
        refreshSessionRepository.save(newSession);
        return new IssuedRefreshToken(rawToken, maxAgeSeconds);
    }

    private void markSessionRevokedIfNeeded(RefreshSession session, LocalDateTime now) {
        if (session.getRevokedAt() == null) {
            session.setRevokedAt(now);
            session.setLastUsedAt(now);
            refreshSessionRepository.save(session);
        }
    }

    private LocalDateTime nowUtc() {
        return LocalDateTime.ofInstant(Instant.now(), STORAGE_ZONE);
    }

    private UnauthorizedException invalidRefreshToken(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieService.buildClearCookieHeader());
        return new UnauthorizedException("ERRORS.AUTH.INVALID_CREDENTIALS");
    }

    private String generateRefreshToken() {
        byte[] randomBytes = new byte[48];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashRefreshToken(String rawRefreshToken) {
        if (!StringUtils.hasText(rawRefreshToken)) {
            throw new UnauthorizedException("ERRORS.AUTH.INVALID_CREDENTIALS");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawRefreshToken.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }

    private String normalizeEmail(String rawEmail) {
        return rawEmail == null ? "" : rawEmail.trim().toLowerCase(Locale.ROOT);
    }

    private record IssuedRefreshToken(String rawToken, long maxAgeSeconds) {
    }
}
