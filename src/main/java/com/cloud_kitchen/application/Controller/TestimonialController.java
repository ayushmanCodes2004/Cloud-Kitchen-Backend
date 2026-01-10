package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.ApiResponse;
import com.cloud_kitchen.application.DTO.TestimonialRequest;
import com.cloud_kitchen.application.DTO.TestimonialResponse;
import com.cloud_kitchen.application.Security.UserPrincipal;
import com.cloud_kitchen.application.Service.TestimonialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/testimonials", "/api/testimonials"})
@RequiredArgsConstructor
public class TestimonialController {

    private final TestimonialService testimonialService;

    @PostMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'CHEF')")
    public ResponseEntity<ApiResponse<TestimonialResponse>> submitTestimonial(@Valid @RequestBody TestimonialRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            Long userId = userPrincipal.getId();
            String userType = userPrincipal.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .orElse("STUDENT");

            TestimonialResponse response = testimonialService.submitTestimonial(userId, userType, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Testimonial submitted successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred while submitting testimonial", null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'CHEF')")
    public ResponseEntity<ApiResponse<TestimonialResponse>> updateTestimonial(
            @PathVariable Long id,
            @Valid @RequestBody TestimonialRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            Long userId = userPrincipal.getId();
            String userType = userPrincipal.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .orElse("STUDENT");

            TestimonialResponse response = testimonialService.updateTestimonial(id, userId, userType, request);
            return ResponseEntity.ok(new ApiResponse<>(true, "Testimonial updated successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred while updating testimonial", null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'CHEF')")
    public ResponseEntity<ApiResponse<Void>> deleteTestimonial(@PathVariable Long id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            Long userId = userPrincipal.getId();
            String userType = userPrincipal.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .orElse("STUDENT");

            testimonialService.deleteTestimonial(id, userId, userType);
            return ResponseEntity.ok(new ApiResponse<>(true, "Testimonial deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred while deleting testimonial", null));
        }
    }

    @GetMapping("/approved")
    public ResponseEntity<ApiResponse<List<TestimonialResponse>>> getApprovedTestimonials() {
        try {
            List<TestimonialResponse> testimonials = testimonialService.getApprovedTestimonials();
            return ResponseEntity.ok(new ApiResponse<>(true, "Approved testimonials fetched successfully", testimonials));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred while fetching testimonials", null));
        }
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('STUDENT', 'CHEF')")
    public ResponseEntity<ApiResponse<TestimonialResponse>> getMyTestimonial() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            Long userId = userPrincipal.getId();
            String userType = userPrincipal.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replace("ROLE_", ""))
                    .orElse("STUDENT");

            TestimonialResponse response = testimonialService.getMyTestimonial(userId, userType);
            if (response == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "No testimonial found", null));
            }
            return ResponseEntity.ok(new ApiResponse<>(true, "Testimonial retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred while fetching testimonial", null));
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<TestimonialResponse>>> getPendingTestimonials() {
        try {
            List<TestimonialResponse> testimonials = testimonialService.getPendingTestimonials();
            return ResponseEntity.ok(new ApiResponse<>(true, "Pending testimonials retrieved successfully", testimonials));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred while fetching pending testimonials", null));
        }
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TestimonialResponse>> approveTestimonial(@PathVariable Long id) {
        try {
            TestimonialResponse response = testimonialService.approveTestimonial(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Testimonial approved successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred while approving testimonial", null));
        }
    }

    @DeleteMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> rejectTestimonial(@PathVariable Long id) {
        try {
            testimonialService.rejectTestimonial(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Testimonial rejected successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred while rejecting testimonial", null));
        }
    }
}
