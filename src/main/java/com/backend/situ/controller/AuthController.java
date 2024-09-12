package com.backend.situ.controller;

import com.backend.situ.entity.Company;
import com.backend.situ.entity.User;
import com.backend.situ.enums.UserRole;
import com.backend.situ.model.ChangePasswordDTO;
import com.backend.situ.model.LoginDTO;
import com.backend.situ.model.SessionDTO;
import com.backend.situ.model.SignupDTO;
import com.backend.situ.service.AuthService;
import com.backend.situ.service.CompanyService;
import com.backend.situ.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/api/situ/auth")
public class AuthController {
    @Autowired
    private final AuthService authService;

    @Autowired
    private final CompanyService companyService;

    @Autowired
    private final UserService userService;

    @Autowired
    public AuthController(AuthService authService, CompanyService companyService, UserService userService) {
        this.authService = authService;
        this.companyService = companyService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<SessionDTO> login(
            @RequestBody LoginDTO form,
            HttpServletResponse response
    ) {
        SessionDTO session = this.authService.doLogin(form, response);

        if (session == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(session);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        this.authService.destroyCookie(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signup")
    public ResponseEntity<HashMap<String, String>> signup(@RequestBody SignupDTO form) {
        Company company;
        User user;
        HashMap<String, String> resp = new HashMap<>();

        // Se verifica si no existe la empresa
        if (this.companyService.existCompanyName(form.companyName())) {
            resp.put("Error", "La empresa ya está registrada");
            return ResponseEntity.status(400).body(resp);
        }
        else {
            company = new Company(form.companyName());
            company = this.companyService.createCompany(company);
        }

        // Se verifica si no existe el usuario
        if (this.userService.existDNI(form.dni())) {
            resp.put("Error", "DNI ya ocupado en una empresa");
            return ResponseEntity.status(400).body(resp);
        }
        else{
            user = new User(
                    company,
                    form.dni(),
                    form.firstName(),
                    form.lastName(),
                    UserRole.ADMIN
            );
            user = this.userService.createUser(user);
        }

        resp = this.authService.signup(form, user);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordDTO form) {
        boolean isChanged = this.authService.changePassword(form);

        if (!isChanged) {
            return ResponseEntity.status(400).body("Credenciales invalidas");
        }

        return ResponseEntity.ok("¡Contraseña modificada con éxito!");
    }

    @GetMapping("/get-session")
    public ResponseEntity<SessionDTO> getSession(@CookieValue("authToken") String authToken){
        SessionDTO session = this.authService.getSessionData(authToken);

        if (session == null) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(session);
    }
}
