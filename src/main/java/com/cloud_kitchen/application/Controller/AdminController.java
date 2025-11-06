package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.ApiResponse;
import com.cloud_kitchen.application.DTO.UserResponse;
import com.cloud_kitchen.application.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@RestController
//@RequestMapping("/api/admin")
//@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")
//public class AdminController {
//
//    private final UserService userService;
//
//    @GetMapping("/users")
//    public ResponseEntity<ApiResponse<R>> getAllUsers() {
//        try {
//            List<UserResponse> users = userService.getAllUsers();
//            return ResponseEntity.ok(new ApiResponse<R>(true, "Users fetched successfully", users));
//        } catch (Exception e) {
//            return ResponseEntity.status(500)
//                    .body(new ApiResponse<R>(false, e.getMessage()));
//        }
//    }
//
//    @GetMapping("/users/{id}")
//    public ResponseEntity<ApiResponse<R>> getUserById(@PathVariable Long id) {
//        try {
//            UserResponse user = userService.getUserById(id);
//            return ResponseEntity.ok(new ApiResponse<R>(true, "User fetched successfully", user));
//        } catch (Exception e) {
//            return ResponseEntity.status(404)
//                    .body(new ApiResponse<R>(false, e.getMessage()));
//        }
//    }

//    @PatchMapping("/users/{id}/deactivate")
//    public ResponseEntity<ApiResponse<R>> deactivateUser(@PathVariable Long id) {
//        try {
//            UserResponse user = userService.deactivateUser(id);
//            return ResponseEntity.ok(new ApiResponse<R>(true, "User deactivated successfully", user));
//        } catch (Exception e) {
//            return ResponseEntity.status(400)
//                    .body(new ApiResponse<R>(false, e.getMessage()));
//        }
//    }

//    @PatchMapping("/users/{id}/activate")
//    public ResponseEntity<ApiResponse<R>> activateUser(@PathVariable Long id) {
//        try {
//            UserResponse user = userService.activateUser(id);
//            return ResponseEntity.ok(new ApiResponse<R>(true, "User activated successfully", user));
//        } catch (Exception e) {
//            return ResponseEntity.status(400)
//                    .body(new ApiResponse<R>(false, e.getMessage()));
//        }
//    }
//
//    @GetMapping("/chefs")
//    public ResponseEntity<ApiResponse<R>> getAllChefs() {
//        try {
//            List<UserResponse> chefs = userService.getAllChefs();
//            return ResponseEntity.ok(new ApiResponse<R>(true, "Chefs fetched successfully", chefs));
//        } catch (Exception e) {
//            return ResponseEntity.status(500)
//                    .body(new ApiResponse<R>(false, e.getMessage()));
//        }
//    }
//
//    @PatchMapping("/chefs/{id}/verify")
//    public ResponseEntity<ApiResponse<R>> verifyChef(@PathVariable Long id) {
//        try {
//            UserResponse chef = userService.verifyChef(id);
//            return ResponseEntity.ok(new ApiResponse<R>(true, "Chef verified successfully", chef));
//        } catch (Exception e) {
//            return ResponseEntity.status(400)
//                    .body(new ApiResponse<R>(false, e.getMessage()));
//        }
//    }
//}

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        try {
            List<UserResponse> users = userService.getAllUsers();
            return ResponseEntity.ok(new ApiResponse<>(true, "Users fetched successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        try {
            UserResponse user = userService.getUserById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "User fetched successfully", user));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable Long id) {
        try {
            UserResponse user = userService.deactivateUser(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "User deactivated successfully", user));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PatchMapping("/users/{id}/activate")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable Long id) {
        try {
            UserResponse user = userService.activateUser(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "User activated successfully", user));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/chefs")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllChefs() {
        try {
            List<UserResponse> chefs = userService.getAllChefs();
            return ResponseEntity.ok(new ApiResponse<>(true, "Chefs fetched successfully", chefs));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PatchMapping("/chefs/{id}/verify")
    public ResponseEntity<ApiResponse<UserResponse>> verifyChef(@PathVariable Long id) {
        try {
            UserResponse chef = userService.verifyChef(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Chef verified successfully", chef));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PatchMapping("/chefs/{id}/unverify")
    public ResponseEntity<ApiResponse<UserResponse>> unverifyChef(@PathVariable Long id) {
        try {
            UserResponse chef = userService.unverifyChef(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Chef unverified successfully", chef));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PatchMapping("/chefs/{id}/toggle-verify")
    public ResponseEntity<ApiResponse<UserResponse>> toggleChefVerification(@PathVariable Long id) {
        try {
            UserResponse chef = userService.toggleChefVerification(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Chef verification toggled successfully", chef));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
