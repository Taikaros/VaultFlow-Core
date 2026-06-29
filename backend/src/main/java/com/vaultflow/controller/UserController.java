package com.vaultflow.controller;

import com.vaultflow.model.User;
import com.vaultflow.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<User>> list(Authentication auth, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(userService.listByCompany(auth.getName(), pageable));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getById(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getById(userId));
    }

    @PostMapping
    public ResponseEntity<User> create(Authentication auth, @RequestBody Map<String, String> body) {
        User user = userService.create(
            auth.getName(),
            body.get("name"),
            body.get("email"),
            body.get("password"),
            body.get("role"),
            body.get("walletId")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<User> update(@PathVariable String userId, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(userService.update(userId, body.get("name"), body.get("role"), body.get("walletId")));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> delete(@PathVariable String userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
