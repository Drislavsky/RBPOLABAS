package com.example.autoservice.controller;

import com.example.autoservice.dto.RegistrationRequest;
import com.example.autoservice.model.User;
import com.example.autoservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request) {
        try {
            if (!isValidRole(request.getRole())) {
                return ResponseEntity.badRequest().body("Invalid role. Allowed roles: ROLE_ADMIN, ROLE_MECHANIC, ROLE_CUSTOMER");
            }

            User user = userService.registerUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getRole()
            );

            return ResponseEntity.ok("User registered successfully: " + user.getUsername());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/password-strength")
    public ResponseEntity<?> checkPasswordStrength(@RequestParam String password) {
        boolean isStrong = userService.validatePasswordStrength(password);
        return ResponseEntity.ok(isStrong ? "STRONG" : "WEAK");
    }

    private boolean isValidRole(String role) {
        return role != null &&
                (role.equals("ROLE_ADMIN") ||
                        role.equals("ROLE_MECHANIC") ||
                        role.equals("ROLE_CUSTOMER"));
    }
}