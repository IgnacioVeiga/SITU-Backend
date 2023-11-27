package com.backend.situ.controller;

import com.backend.situ.entity.User;
import com.backend.situ.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/situ/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list/{pageIndex}/{pageSize}/{companyId}")
    public Page<User> list(@PathVariable("pageIndex") int pageIndex,
                           @PathVariable("pageSize") int pageSize,
                           @PathVariable("companyId") int companyId) {
        return this.userService.listUsers(pageIndex, pageSize,companyId);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<User> get(@PathVariable("id") Long id) {
        User user = this.userService.getUser(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/create")
    public ResponseEntity<Long> create(@RequestBody User user) {
        User userCreated = this.userService.createUser(user);
        if (userCreated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // return the user id
        return ResponseEntity.ok(userCreated.getId());
    }
}
