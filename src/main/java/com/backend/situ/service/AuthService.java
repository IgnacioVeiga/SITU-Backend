package com.backend.situ.service;

import com.backend.situ.entity.UserCredentials;
import com.backend.situ.model.ChangePasswordDTO;
import com.backend.situ.model.LoginDTO;
import com.backend.situ.model.SignupDTO;
import com.backend.situ.repository.AuthRepository;
import com.backend.situ.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class AuthService {

    @Autowired
    private final AuthRepository authRepository;

    @Autowired
    private final JWTUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    public AuthService(AuthRepository authRepository, JWTUtil jwtUtil) {
        this.authRepository = authRepository;
        this.jwtUtil = jwtUtil;
    }

    public String doLogin(LoginDTO form) {
        UserCredentials user = this.authRepository.findByEmail(form.email());

        if (user == null || !passwordEncoder.matches(form.password(), user.encodedPassword)) {
            return null;
        }

        return jwtUtil.getToken(user);
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

    public String signup(SignupDTO form) {
        String randomPassword = generateRandomPassword();
        String encodedPassword = passwordEncoder.encode(randomPassword);

        UserCredentials newUser = new UserCredentials(form.email(), encodedPassword);
//        authRepository.save(newUser);

        // TODO: guardar en la DB el resto de los datos del formulario

        // ¡TEMPORAL! En un entorno de producción, la contraseña se envía por email al usuario.
        return "Email: " + form.email() + " - Password: " + randomPassword;
    }

    public boolean changePassword(ChangePasswordDTO form) {
        // TODO: obtener email desde la cookie con un interceptor y simplificar el DTO
        UserCredentials user = this.authRepository.findByEmail(form.email());

        if (user == null || !passwordEncoder.matches(form.currentPassword(), user.encodedPassword)) {
            return false;
        }

        user.encodedPassword = passwordEncoder.encode(form.newPassword());
        authRepository.save(user);
        return true;
    }
}
