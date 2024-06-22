package com.backend.situ.service;

import com.backend.situ.entity.UserCredentials;
import com.backend.situ.model.ChangePasswordForm;
import com.backend.situ.model.LogInFrom;
import com.backend.situ.model.SignUpForm;
import com.backend.situ.repository.AuthRepository;
import com.backend.situ.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class AuthService {

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private JWTUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public AuthService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public String doLogin(LogInFrom form) {
        UserCredentials user = this.authRepository.findByEmail(form.email());

        if (user == null || !passwordEncoder.matches(form.password(), user.getPassword())) {
            return null;
        }

        return jwtUtil.generateToken(user);
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

    public String signup(SignUpForm form) {
        String randomPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(randomPassword);

        UserCredentials newUser = new UserCredentials(form.email(), encodedPassword);
        authRepository.save(newUser);

        // TODO: guardar en la DB el resto de los datos del formulario

        // ¡TEMPORAL! En un entorno de producción, la contraseña se envía por email al usuario.
        return "Email: " + form.email() + " - Password: " + randomPassword;
    }

    public boolean changePassword(ChangePasswordForm form) {
        UserCredentials user = this.authRepository.findByEmail(form.getEmail());

        if (user == null || !passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
            return false;
        }

        String encodedNewPassword = passwordEncoder.encode(form.getNewPassword());
        user.setPassword(encodedNewPassword);
        authRepository.save(user);
        return true;
    }
}
