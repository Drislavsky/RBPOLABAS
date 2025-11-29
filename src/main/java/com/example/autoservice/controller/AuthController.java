package com.example.autoservice.controller;

import com.example.autoservice.model.*;
import com.example.autoservice.repository.*;
import com.example.autoservice.service.UserService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request) {
        try {
            if (!isValidRole(request.getRole())) {
                return ResponseEntity.badRequest().body("Invalid role");
            }

            // Валидация данных в зависимости от роли
            if ("ROLE_CUSTOMER".equals(request.getRole())) {
                if (request.getName() == null || request.getEmail() == null || request.getPhone() == null) {
                    return ResponseEntity.badRequest().body("For CUSTOMER role: name, email and phone are required");
                }
            } else if ("ROLE_MECHANIC".equals(request.getRole())) {
                if (request.getName() == null || request.getSpecialization() == null) {
                    return ResponseEntity.badRequest().body("For MECHANIC role: name and specialization are required");
                }
            }

            User user = userService.registerUser(
                    request.getUsername(),
                    request.getPassword(),
                    request.getRole()
            );

            // Создаем связанную сущность с данными от админа
            if ("ROLE_CUSTOMER".equals(request.getRole())) {
                Customer customer = new Customer();
                customer.setName(request.getName());
                customer.setEmail(request.getEmail());
                customer.setPhone(request.getPhone());

                Customer savedCustomer = customerRepository.save(customer);
                user.setCustomer(savedCustomer);
                userRepository.save(user);

            } else if ("ROLE_MECHANIC".equals(request.getRole())) {
                Mechanic mechanic = new Mechanic();
                mechanic.setName(request.getName());
                mechanic.setSpecialization(request.getSpecialization());

                Mechanic savedMechanic = mechanicRepository.save(mechanic);
                // Если нужно связать User с Mechanic, добавить связь в User entity
            }

            return ResponseEntity.ok("User registered successfully: " + user.getUsername());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}