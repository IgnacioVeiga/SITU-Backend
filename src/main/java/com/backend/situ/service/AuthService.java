package com.backend.situ.service;

import com.backend.situ.entity.Company;
import com.backend.situ.entity.User;
import com.backend.situ.entity.UserCredentials;
import com.backend.situ.model.ChangePasswordDTO;
import com.backend.situ.model.LoginDTO;
import com.backend.situ.model.SessionDTO;
import com.backend.situ.model.SignupDTO;
import com.backend.situ.repository.AuthRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;

@Service
public class AuthService {

    @Autowired
    private final AuthRepository authRepository;

    @Autowired
    private final JWTService jwtService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public AuthService(AuthRepository authRepository, JWTService jwtService) {
        this.authRepository = authRepository;
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

        // Configura la cookie con el token
        Cookie cookie = new Cookie("authToken", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        if (form.rememberMe()) {
            cookie.setMaxAge(720 * 60 * 60); // 720 horas (30 días)
        } else {
            cookie.setMaxAge(-1); // Sesión actual del navegador
        }
        response.addCookie(cookie);

        return this.getSessionData(userCred);
    }

    public String generateRandomPassword() {
        int length = 10;
        String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(charSet.length());
            password.append(charSet.charAt(randomIndex));
        }

        return password.toString();
    }

    public HashMap<String, String> signup(SignupDTO form, User user) {
        String randomPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(randomPassword);

        UserCredentials newUser = new UserCredentials(user, form.email(), encodedPassword);
        this.authRepository.save(newUser);

        // TODO: aún no hay nada definido que hacer con el teléfono y las notas recibidas del formulario

        // ¡TEMPORAL! En un entorno de producción, la contraseña se envía por email al usuario.
        HashMap<String, String> resp = new HashMap<>();
        resp.put("Email", form.email());
        resp.put("Password", randomPassword);
        return resp;
    }

    public boolean changePassword(ChangePasswordDTO form) {
        UserCredentials user = this.authRepository.findByEmail(form.email()).orElse(null);

        if (user == null || !passwordEncoder.matches(form.currentPassword(), user.getPassword())) {
            return false;
        }

        String newEncodedPassword = passwordEncoder.encode(form.newPassword());
        user.setEncodedPassword(newEncodedPassword);
        this.authRepository.save(user);
        return true;
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
                userCredentials.getPassword(),
                user.getFirstName() + " " + user.getLastName(),
                user.getRole()
        );
    }

    public void destroyCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("authToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Elimina la cookie
        response.addCookie(cookie);
    }
}
