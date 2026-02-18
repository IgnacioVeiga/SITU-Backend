package com.backend.situ.controller;

import com.backend.situ.model.ApiResponse;
import com.backend.situ.model.UserCreateDTO;
import com.backend.situ.model.UserResponseDTO;
import com.backend.situ.model.UserUpdateDTO;
import com.backend.situ.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{pageIndex}/{pageSize}")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> list(
            @PathVariable("pageIndex") Integer pageIndex,
            @PathVariable("pageSize") Integer pageSize,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        return ResponseEntity.ok(ApiResponse.success(this.userService.listUsers(pageIndex, pageSize, subjectEmail), null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> get(
            @PathVariable("id") Long id,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        return ResponseEntity.ok(ApiResponse.success(this.userService.getUser(id, subjectEmail), null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponseDTO>> create(
            @RequestBody UserCreateDTO request,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        UserResponseDTO userCreated = this.userService.createUser(request, subjectEmail);
        return ResponseEntity.ok(ApiResponse.success(userCreated, "Usuario creado."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDTO>> update(
            @PathVariable("id") Long id,
            @RequestBody UserUpdateDTO request,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        UserResponseDTO updated = this.userService.updateUser(id, request, subjectEmail);
        return ResponseEntity.ok(ApiResponse.success(updated, "Usuario actualizado."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable("id") Long id,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        this.userService.deleteUser(id, subjectEmail);
        return ResponseEntity.ok(ApiResponse.success(null, "Usuario eliminado."));
    }
}
