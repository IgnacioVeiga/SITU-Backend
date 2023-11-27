package com.backend.situ.controller;

import com.backend.situ.entity.LogInCredentials;
import com.backend.situ.model.SignUpForm;
import com.backend.situ.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("situ/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Integer> login(@RequestBody LogInCredentials cred) {
        // returns userId
        return ResponseEntity.ok(this.authService.doLogin(cred));
    }

    @PostMapping("/signup")
    public boolean signup(@RequestBody SignUpForm form) {
        return this.authService.signup(form);
    }
}
