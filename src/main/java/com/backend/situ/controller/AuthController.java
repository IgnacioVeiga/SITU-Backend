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

    // TODO: must use @Transactional?
    @PostMapping("/signup")
    public ResponseEntity<HashMap<String, String>> signup(@RequestBody SignupDTO form) {
        Company company;
        User user;
        HashMap<String, String> resp = new HashMap<>();

        // Se verifica si no existe la empresa
        if (this.companyService.existCompanyName(form.companyName())) {
            resp.put("message", "La empresa ya está registrada");
            return ResponseEntity.status(400).body(resp);
        }
        else {
            company = new Company(form.companyName());
        }

        // Se verifica si no existe el usuario
        if (this.userService.existDNI(form.dni())) {
            resp.put("message", "DNI ya ocupado en una empresa");
            return ResponseEntity.status(400).body(resp);
        }
        else{
            company = this.companyService.createCompany(company);
            user = new User(
                    company,
                    form.dni(),
                    form.firstName(),
                    form.lastName(),
                    UserRole.ADMIN
            );
            user = this.userService.createUser(user);
        }

        this.authService.signup(form, user);
        resp.put("message", "¡Registro exitoso!");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/password")
    public ResponseEntity<HashMap<String, String>> updatePassword(
            @RequestBody ChangePasswordDTO form,
            @CookieValue("authToken") String authToken
    ) {
        HashMap<String, String> resp = new HashMap<>();
        int statusCode = this.authService.changePassword(authToken, form);

        // TODO: refactor
        switch (statusCode) {
            case 200:
                resp.put("message", "Contraseña modificada");
                break;
            case 400:
                resp.put("message", "Contraseña actual incorrecta");
                break;
            case 404:
                resp.put("message", "Usuario no encontrado");
                break;
            default:
                resp.put("message", "Hubo un error desconocido");
                break;
        }
        return ResponseEntity.status(statusCode).body(resp);
    }

    @GetMapping("/session")
    public ResponseEntity<SessionDTO> getSession(@CookieValue("authToken") String authToken){
        SessionDTO session = this.authService.getSessionData(authToken);

        if (session == null) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(null);
        }

        return ResponseEntity.ok(session);
    }
}
