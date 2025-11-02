package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.ApiResponse;
import com.cloud_kitchen.application.DTO.UserResponse;
import com.cloud_kitchen.application.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getAllUsers() {
        try {
            List<UserResponse> users = userService.getAllUsers();
            return ResponseEntity.ok(new ApiResponse(true, "Users fetched successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = userService.getUserById(id);
            return ResponseEntity.ok(new ApiResponse(true, "User fetched successfully", user));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<ApiResponse> deactivateUser(@PathVariable Long id) {
        try {
            UserResponse user = userService.deactivateUser(id);
            return ResponseEntity.ok(new ApiResponse(true, "User deactivated successfully", user));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse> activateUser(@PathVariable Long id) {
        try {
            UserResponse user = userService.activateUser(id);
            return ResponseEntity.ok(new ApiResponse(true, "User activated successfully", user));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/chefs")
    public ResponseEntity<ApiResponse> getAllChefs() {
        try {
            List<UserResponse> chefs = userService.getAllChefs();
            return ResponseEntity.ok(new ApiResponse(true, "Chefs fetched successfully", chefs));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PatchMapping("/chefs/{id}/verify")
    public ResponseEntity<ApiResponse> verifyChef(@PathVariable Long id) {
        try {
            UserResponse chef = userService.verifyChef(id);
            return ResponseEntity.ok(new ApiResponse(true, "Chef verified successfully", chef));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}
