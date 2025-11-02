package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.*;
import com.cloud_kitchen.application.Service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse> registerStudent(@Valid @RequestBody StudentRegistrationRequest request) {
        try {
            AuthResponse authResponse = authService.registerStudent(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Student registered successfully", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/register/chef")
    public ResponseEntity<ApiResponse> registerChef(@Valid @RequestBody ChefRegistrationRequest request) {
        try {
            AuthResponse authResponse = authService.registerChef(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Chef registered successfully", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/register/admin")
    public ResponseEntity<ApiResponse> registerAdmin(@Valid @RequestBody AdminRegistrationRequest request) {
        try {
            AuthResponse authResponse = authService.registerAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Admin registered successfully", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse authResponse = authService.login(request);
            return ResponseEntity.ok(new ApiResponse(true, "Login successful", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid email or password"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getCurrentUser() {
        try {
            return ResponseEntity.ok(new ApiResponse(true, "User fetched successfully",
                    authService.getCurrentUser()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}

