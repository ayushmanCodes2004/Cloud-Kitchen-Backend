package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.ApiResponse;
import com.cloud_kitchen.application.Entity.Student;
import com.cloud_kitchen.application.Entity.User;
import com.cloud_kitchen.application.Repository.StudentRepository;
import com.cloud_kitchen.application.Repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostic")
@Slf4j
public class DiagnosticController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/check-student")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkStudent() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Map<String, Object> info = new HashMap<>();
            
            // User info
            info.put("userId", user.getId());
            info.put("userEmail", user.getEmail());
            info.put("userRole", user.getRole().toString());
            info.put("userName", user.getName());
            
            // Check if student exists
            boolean studentExists = studentRepository.existsById(user.getId());
            info.put("studentRecordExists", studentExists);
            
            if (studentExists) {
                Student student = studentRepository.findById(user.getId()).orElse(null);
                if (student != null) {
                    info.put("studentId", student.getStudentId());
                    info.put("college", student.getCollege());
                    info.put("hostelName", student.getHostelName());
                    info.put("studentEmail", student.getEmail());
                }
            } else {
                info.put("error", "Student record not found in students table");
                info.put("solution", "User exists but student record is missing. Check registration process.");
            }
            
            log.info("Diagnostic check for user {}: {}", user.getEmail(), info);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Diagnostic info retrieved", info));
        } catch (Exception e) {
            log.error("Error in diagnostic check", e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Diagnostic check failed", errorInfo));
        }
    }
}
