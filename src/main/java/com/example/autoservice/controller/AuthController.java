package com.example.autoservice.controller;

import com.example.autoservice.model.*;
import com.example.autoservice.repository.*;
import com.example.autoservice.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final MechanicRepository mechanicRepository;

    public AuthController(UserService userService, UserRepository userRepository,
                          CustomerRepository customerRepository, MechanicRepository mechanicRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.mechanicRepository = mechanicRepository;
    }

    private boolean isValidRole(String role) {
        return "ROLE_CUSTOMER".equals(role) || "ROLE_MECHANIC".equals(role) || "ROLE_ADMIN".equals(role);
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request) {
        try {
            if (!isValidRole(request.getRole())) {
                return ResponseEntity.badRequest().body("Invalid role");
            }

            if ("ROLE_CUSTOMER".equals(request.getRole())) {
                if (request.getName() == null || request.getEmail() == null || request.getPhone() == null) {
                    return ResponseEntity.badRequest().body("For CUSTOMER role: name, email and phone are required");
                }
                if (customerRepository.existsByEmail(request.getEmail())) {
                    return ResponseEntity.badRequest().body("Email already exists");
                }
                if (customerRepository.existsByPhone(request.getPhone())) {
                    return ResponseEntity.badRequest().body("Phone already exists");
                }
            } else if ("ROLE_MECHANIC".equals(request.getRole())) {
                if (request.getName() == null || request.getSpecialization() == null) {
                    return ResponseEntity.badRequest().body("For MECHANIC role: name and specialization are required");
                }
            }

            User user;

            if ("ROLE_CUSTOMER".equals(request.getRole())) {
                // Сначала создаём Customer
                Customer customer = new Customer(request.getName(), request.getPhone(), request.getEmail());
                Customer savedCustomer = customerRepository.save(customer);

                // Затем User и связываем
                user = userService.registerUser(request.getUsername(), request.getPassword(), request.getRole());
                user.setCustomer(savedCustomer);
                userRepository.save(user);

            } else if ("ROLE_MECHANIC".equals(request.getRole())) {
                // Сначала создаём User
                user = userService.registerUser(request.getUsername(), request.getPassword(), request.getRole());

                // Затем Mechanic и связываем с User
                Mechanic mechanic = new Mechanic();
                mechanic.setName(request.getName());
                mechanic.setSpecialization(request.getSpecialization());
                mechanic.setUser(user);
                mechanicRepository.save(mechanic);

            } else {
                // Для ADMIN просто создаём User
                user = userService.registerUser(request.getUsername(), request.getPassword(), request.getRole());
            }

            return ResponseEntity.ok("User registered successfully: " + user.getUsername());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Registration failed: " + e.getMessage());
        }
    }
}