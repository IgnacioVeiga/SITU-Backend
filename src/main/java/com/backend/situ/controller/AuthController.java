package com.backend.situ.controller;

import com.backend.situ.enums.UserRole;
import com.backend.situ.model.ChangePasswordDTO;
import com.backend.situ.model.LoginDTO;
import com.backend.situ.model.SessionDTO;
import com.backend.situ.model.SignupDTO;
import com.backend.situ.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("situ/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<SessionDTO> login(
            @RequestBody LoginDTO form,
            HttpServletResponse response
    ) {
        String token = this.authService.doLogin(form);
        if (token != null) {
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

            // TEMPORAL, implementar y refactorizar las funciones necesarias
            SessionDTO session = this.authService.getSessionData(1);

            return ResponseEntity.ok(session);
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
    public ResponseEntity<String> signup(@RequestBody SignupDTO form) {
        String tempResponse = this.authService.signup(form);
        return ResponseEntity.ok(tempResponse);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDTO form) {
        boolean isChanged = this.authService.changePassword(form);
        if (isChanged) {
            return ResponseEntity.ok("¡Contraseña modificada con éxito!");
        } else {
            return ResponseEntity.status(400).body("Credenciales invalidas");
        }
    }

    @GetMapping("/get-session")
    public ResponseEntity<SessionDTO> getSession(){
        // TODO: get from cookie
        Integer userId = 1;
        SessionDTO session = this.authService.getSessionData(userId);

        if (session != null) {
            return ResponseEntity.ok(session);
        }
        else {
            return ResponseEntity.status(404).body(null);
        }
    }
}
