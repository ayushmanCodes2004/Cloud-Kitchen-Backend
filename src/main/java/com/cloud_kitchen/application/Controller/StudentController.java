package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.ApiResponse;
import com.cloud_kitchen.application.DTO.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<String>> getDashboard() {
        return ResponseEntity.ok(new ApiResponse<>(true, "Student dashboard",
                "Welcome to Student Dashboard"));
    }
}
