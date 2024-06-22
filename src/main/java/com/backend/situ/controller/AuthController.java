package com.backend.situ.controller;

import com.backend.situ.entity.UserCredentials;
import com.backend.situ.model.ChangePasswordForm;
import com.backend.situ.model.SignUpForm;
import com.backend.situ.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<Map<String, String>> login(@RequestBody UserCredentials cred) {
        String token = this.authService.doLogin(cred);
        if (token != null) {
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(null);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<Boolean> signup(@RequestBody SignUpForm form) {
        boolean isSignedUp = this.authService.signup(form);
        return ResponseEntity.ok(isSignedUp);
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
