package com.backend.situ.service;

import com.backend.situ.entity.Company;
import com.backend.situ.entity.User;
import com.backend.situ.entity.UserCredentials;
import com.backend.situ.model.ChangePasswordDTO;
import com.backend.situ.model.LoginDTO;
import com.backend.situ.model.SessionDTO;
import com.backend.situ.model.SignupDTO;
import com.backend.situ.repository.AuthRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;

@Service
public class AuthService {
    @Value("${cookie.secure}")
    private boolean secureCookie;

    @Autowired
    private final AuthRepository authRepository;

    @Autowired
    private final EmailService emailService;

    @Autowired
    private final JWTService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public AuthService(AuthRepository authRepository, EmailService emailService, JWTService jwtService) {
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
        if (token == null) return null;
        addAuthCookie(response, token);

        return this.getSessionData(userCred);
    }

    public String generateRandomPassword() {
        int length = 10;
        SecureRandom random = new SecureRandom();
        byte[] passwordBytes = new byte[length];
        random.nextBytes(passwordBytes);

        StringBuilder password = new StringBuilder();
        for (byte b : passwordBytes) {
            password.append((char) ((b & 0x7F) % 26 + 'a')); // Limita a caracteres alfabéticos
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
            System.out.println(e.getMessage());
        }
        // TODO: Aún no hay nada definido que hacer con el teléfono y las notas recibidas del formulario
    }

    public int changePassword(String authToken, ChangePasswordDTO form) {
        String email = jwtService.getSubjectFromToken(authToken);
        UserCredentials user = this.authRepository.findByEmail(email).orElse(null);
        if (user == null) return HttpServletResponse.SC_NOT_FOUND;

        if (form.currentPassword().isEmpty()) return HttpServletResponse.SC_BAD_REQUEST;

        if (!passwordEncoder.matches(form.currentPassword(), user.getPassword())) {
            return HttpServletResponse.SC_BAD_REQUEST;
        }

        String newEncodedPassword = passwordEncoder.encode(form.newPassword());
        user.setEncodedPassword(newEncodedPassword);
        this.authRepository.save(user);
        return HttpServletResponse.SC_OK;
    }

    public SessionDTO getSessionData(String authToken) {
        if (jwtService.isTokenExpired(authToken)) return null;

        String email = jwtService.getSubjectFromToken(authToken);
        UserCredentials userCredentials = this.authRepository.findByEmail(email).orElse(null);

        if (userCredentials == null) return null;

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
                .sameSite("Lax")  // "None" - "Lax" - "Strict"
                .path("/")
                .maxAge(Duration.ofDays(1))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void destroyCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("authToken", "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")  // "None" - "Lax" - "Strict"
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
            this.addAuthCookie(response, newToken);
        }
        return true;
    }
}
