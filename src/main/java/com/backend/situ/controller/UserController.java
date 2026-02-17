package com.backend.situ.controller;

import com.backend.situ.entity.User;
import com.backend.situ.exception.BadRequestException;
import com.backend.situ.model.ApiResponse;
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

    @GetMapping("/{pageIndex}/{pageSize}/{companyId}")
    public ResponseEntity<ApiResponse<Page<User>>> list(
            @PathVariable("pageIndex") Integer pageIndex,
            @PathVariable("pageSize") Integer pageSize,
            @PathVariable("companyId") Long companyId
    ) {
        return ResponseEntity.ok(ApiResponse.success(this.userService.listUsers(pageIndex, pageSize, companyId), null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> get(@PathVariable("id") Long id) {
        User user = this.userService.getUser(id);
        if (user == null) {
            throw new BadRequestException("ERRORS.USER.NOT_FOUND");
        }

        return ResponseEntity.ok(ApiResponse.success(user, null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> create(@RequestBody User user) {
        User userCreated = this.userService.createUser(user);
        return ResponseEntity.ok(ApiResponse.success(userCreated.getId(), "Usuario creado."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> update(@PathVariable("id") Long id, @RequestBody User user) {
        User updated = this.userService.updateUser(id, user);
        return ResponseEntity.ok(ApiResponse.success(updated, "Usuario actualizado."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        this.userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Usuario eliminado."));
    }
}
