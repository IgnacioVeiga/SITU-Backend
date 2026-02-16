package com.backend.situ.service;

import com.backend.situ.entity.Company;
import com.backend.situ.entity.User;
import com.backend.situ.entity.UserCredentials;
import com.backend.situ.enums.AuditAction;
import com.backend.situ.enums.UserRole;
import com.backend.situ.event.AuditEvent;
import com.backend.situ.model.ChangePasswordDTO;
import com.backend.situ.model.LoginDTO;
import com.backend.situ.model.SessionDTO;
import com.backend.situ.model.SignupDTO;
import com.backend.situ.repository.AuthRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private static final String PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789!@#$%^&*";
    private static final int GENERATED_PASSWORD_LENGTH = 16;

    @Value("${security.cookie.secure:false}")
    private boolean secureCookie;

    @Value("${security.cookie.max-age-hours:24}")
    private long cookieMaxAgeHours;

    private final ApplicationEventPublisher eventPublisher;
    private final AuthRepository authRepository;
    private final EmailService emailService;
    private final JWTService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public AuthService(
            ApplicationEventPublisher eventPublisher,
            AuthRepository authRepository,
            EmailService emailService,
            JWTService jwtService
    ) {
        this.eventPublisher = eventPublisher;
        this.authRepository = authRepository;
        this.emailService = emailService;
        this.jwtService = jwtService;
    }

    public SessionDTO doLogin(LoginDTO form, HttpServletResponse response) {
        UserCredentials userCred = this.authRepository.findByEmail(form.email()).orElse(null);

        if (userCred == null || !passwordEncoder.matches(form.password(), userCred.getPassword())) {
            return null;
        }

        String token = jwtService.getToken(userCred);
        if (token == null) {
            return null;
        }

        addAuthCookie(response, token);

        String details = "New login by user: " + userCred.getUsername();
        AuditEvent auditEvent = new AuditEvent(this, AuditAction.LOGIN, userCred.getUsername(), details);
        eventPublisher.publishEvent(auditEvent);

        return this.getSessionData(userCred);
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

        UserCredentials newUser = new UserCredentials(user, form.email(), encodedPassword);
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

        // TODO: Nothing definite yet to do with the phone and the notes received from the form.
    }

    public int changePassword(String authToken, ChangePasswordDTO form) {
        String email = jwtService.getSubjectFromToken(authToken);
        UserCredentials user = this.authRepository.findByEmail(email).orElse(null);
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

    public UserRole getRoleFromToken(String authToken) {
        String email = jwtService.getSubjectFromToken(authToken);
        if (email == null || email.isBlank()) {
            return null;
        }

        UserCredentials userCredentials = this.authRepository.findByEmail(email).orElse(null);
        if (userCredentials == null || userCredentials.getUser() == null) {
            return null;
        }

        return userCredentials.getUser().getRole();
    }

    public SessionDTO getSessionData(String authToken) {
        if (jwtService.isTokenExpired(authToken)) {
            return null;
        }

        String email = jwtService.getSubjectFromToken(authToken);
        UserCredentials userCredentials = this.authRepository.findByEmail(email).orElse(null);

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

    public void addAuthCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("authToken", token)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite((secureCookie) ? "None" : "Lax")
                .path("/")
                .maxAge(resolveCookieMaxAge())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void destroyCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("authToken", "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite((secureCookie) ? "None" : "Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public boolean validateAndRenewToken(String authToken, HttpServletResponse response) {
        if (jwtService.isTokenExpired(authToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        if (jwtService.isTokenNearExpiry(authToken)) {
            String newToken = jwtService.renewToken(authToken);
            if (newToken == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
            this.addAuthCookie(response, newToken);
        }
        return true;
    }

    private Duration resolveCookieMaxAge() {
        long safeHours = Math.max(1L, cookieMaxAgeHours);
        return Duration.ofHours(safeHours);
    }
}
