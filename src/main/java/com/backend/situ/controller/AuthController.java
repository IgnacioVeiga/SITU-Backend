package com.backend.situ.controller;

import com.backend.situ.entity.UserCredentials;
import com.backend.situ.model.ChangePasswordForm;
import com.backend.situ.model.LogInFrom;
import com.backend.situ.model.SignUpForm;
import com.backend.situ.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("situ/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestBody LogInFrom form,
            HttpServletResponse response
    ) {
        String token = this.authService.doLogin(form);
        if (token != null) {
            // Configura la cookie con el token
            Cookie cookie = new Cookie("authToken", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            if (form.rememberMe()) {
                cookie.setMaxAge(24 * 60 * 60); // 24 horas
            } else {
                cookie.setMaxAge(-1); // Sesión actual del navegador
            }
            response.addCookie(cookie);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("authToken", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Elimina la cookie
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignUpForm form) {
        String tempResponse = this.authService.signup(form);
        return ResponseEntity.ok(tempResponse);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordForm form) {
        boolean isChanged = this.authService.changePassword(form);
        if (isChanged) {
            return ResponseEntity.ok("¡Contraseña modificada con éxito!");
        } else {
            return ResponseEntity.status(400).body("Credenciales invalidas");
        }
    }
}
