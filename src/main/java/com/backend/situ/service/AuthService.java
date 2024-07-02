package com.backend.situ.service;

import com.backend.situ.entity.UserCredentials;
import com.backend.situ.enums.UserRole;
import com.backend.situ.model.ChangePasswordDTO;
import com.backend.situ.model.LoginDTO;
import com.backend.situ.model.SessionDTO;
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

    public SessionDTO getSessionData(Integer userId){
        return new SessionDTO(
                1,
                1,
                "data:image/png;base64,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",
                "jhon.doe@example.com",
                "Jhon Doe",
                UserRole.ADMIN
        );
    }
}
