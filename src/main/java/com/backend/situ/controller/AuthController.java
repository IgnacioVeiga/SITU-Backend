package com.backend.situ.controller;

import com.backend.situ.entity.Company;
import com.backend.situ.entity.User;
import com.backend.situ.enums.UserRole;
import com.backend.situ.exception.BadRequestException;
import com.backend.situ.exception.UnauthorizedException;
import com.backend.situ.model.*;
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
    public ResponseEntity<ApiResponse<SessionDTO>> login(
            @RequestBody LoginDTO form,
            HttpServletResponse response
    ) {
        SessionDTO session = this.authService.doLogin(form, response);

        if (session == null) {
            throw new UnauthorizedException("ERRORS.AUTH.INVALID_CREDENTIALS");
        }
        // TODO: translate
        return ResponseEntity.ok(ApiResponse.success(session, "Inicio de sesión exitoso."));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        this.authService.destroyCookie(response);
        return ResponseEntity.ok().build();
    }

    // must use @Transactional?
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody SignupDTO form) {
        if (this.companyService.existCompanyName(form.companyName())) {
            throw new BadRequestException("ERRORS.AUTH.COMPANY_EXISTS");
        }
        if (this.userService.existDNI(form.dni())) {
            throw new BadRequestException("ERRORS.AUTH.DNI_EXISTS");
        }

        Company company = this.companyService.createCompany(new Company(form.companyName()));
        User user = this.userService.createUser(new User(
                company,
                form.dni(),
                form.firstName(),
                form.lastName(),
                UserRole.ADMIN
        ));
        this.authService.signup(form, user);
        
        return ResponseEntity.ok(ApiResponse.success(null, "¡Registro exitoso!"));
    }

    @PostMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @RequestBody ChangePasswordDTO form,
            @CookieValue("authToken") String authToken
    ) {
        int statusCode = authService.changePassword(authToken, form);
        // TODO: translate
        return switch (statusCode) {
            case 200 -> ResponseEntity.ok(ApiResponse.success(null, "Contraseña modificada."));
            case 400 -> throw new BadRequestException("ERRORS.AUTH.PASSWORD_INCORRECT");
            case 404 -> throw new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND");
            default -> throw new RuntimeException("ERRORS.GENERIC");
        };
    }

    @GetMapping("/session")
    public ResponseEntity<ApiResponse<SessionDTO>> getSession(@CookieValue("authToken") String authToken) {
        SessionDTO session = this.authService.getSessionData(authToken);

        if (session == null) {
            // TODO: translate
            throw new UnauthorizedException("Por favor, inicie sesión.");
        }

        return ResponseEntity.ok(ApiResponse.success(session, null));
    }
}
