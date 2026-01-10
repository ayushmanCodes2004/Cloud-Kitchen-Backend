package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.*;
import com.cloud_kitchen.application.Entity.Chef;
import com.cloud_kitchen.application.Entity.User;
import com.cloud_kitchen.application.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/auth", "/api/auth"})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/student")
    public ResponseEntity<ApiResponse<AuthResponse>> registerStudent(@Valid @RequestBody StudentRegistrationRequest request) {
        try {
            AuthResponse authResponse = authService.registerStudent(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Student registered successfully", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/register/chef")
    public ResponseEntity<ApiResponse<AuthResponse>> registerChef(@Valid @RequestBody ChefRegistrationRequest request) {
        try {
            AuthResponse authResponse = authService.registerChef(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Chef registered successfully", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/register/admin")
    public ResponseEntity<ApiResponse<AuthResponse>> registerAdmin(@Valid @RequestBody AdminRegistrationRequest request) {
        try {
            AuthResponse authResponse = authService.registerAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Admin registered successfully", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse authResponse = authService.login(request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", authResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Invalid email or password", null));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        try {
            User user = authService.getCurrentUser();
            // UserResponse fields: id, email, name, phoneNumber, role, active, createdAt, verified
            Boolean verified = null;
            if (user instanceof Chef) {
                verified = ((Chef) user).getVerified();
            }
            
            UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.getActive(),
                user.getCreatedAt(),
                verified
            );

            return ResponseEntity.ok(new ApiResponse<>(true, "User fetched successfully", userResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
