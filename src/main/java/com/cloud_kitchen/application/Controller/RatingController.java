package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.ApiResponse;
import com.cloud_kitchen.application.DTO.ChefRatingStats;
import com.cloud_kitchen.application.DTO.MenuItemRatingStats;
import com.cloud_kitchen.application.DTO.RatingRequest;
import com.cloud_kitchen.application.DTO.RatingResponse;
import com.cloud_kitchen.application.Security.UserPrincipal;
import com.cloud_kitchen.application.Service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping("/chef")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<RatingResponse>> rateChef(@Valid @RequestBody RatingRequest request) {
        try {
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            Long studentId = userPrincipal.getId();

            RatingResponse response = ratingService.rateChef(studentId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Chef rated successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred while rating the chef", null));
        }
    }

    @GetMapping("/chef/{chefId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'CHEF', 'ADMIN')")
    public ResponseEntity<ApiResponse<ChefRatingStats>> getChefRatings(@PathVariable Long chefId) {
        try {
            ChefRatingStats stats = ratingService.getChefRatings(chefId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Chef ratings retrieved successfully", stats));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred while fetching ratings", null));
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('STUDENT', 'CHEF', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllRatings() {
        try {
            List<ChefRatingStats> chefRatings = ratingService.getAllChefRatings();
            List<MenuItemRatingStats> menuItemRatings = ratingService.getAllMenuItemRatings();

            Map<String, Object> response = new HashMap<>();
            response.put("chefRatings", chefRatings);
            response.put("menuItemRatings", menuItemRatings);

            return ResponseEntity.ok(new ApiResponse<>(true, "All ratings retrieved successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred while fetching all ratings", null));
        }
    }

    @PostMapping("/menu-item")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<RatingResponse>> rateMenuItem(@Valid @RequestBody RatingRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            Long studentId = userPrincipal.getId();

            RatingResponse response = ratingService.rateMenuItem(studentId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Menu item rated successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred while rating the menu item", null));
        }
    }

    @GetMapping("/menu-item/{menuItemId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'CHEF', 'ADMIN')")
    public ResponseEntity<ApiResponse<MenuItemRatingStats>> getMenuItemRatings(@PathVariable Long menuItemId) {
        try {
            MenuItemRatingStats stats = ratingService.getMenuItemRatings(menuItemId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Menu item ratings retrieved successfully", stats));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred while fetching ratings", null));
        }
    }

    @GetMapping("/my-rated-orders")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<Long>>> getMyRatedOrders() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            Long studentId = userPrincipal.getId();

            List<Long> ratedOrderIds = ratingService.getRatedOrderIdsByStudent(studentId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Rated orders retrieved successfully", ratedOrderIds));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred while fetching rated orders", null));
        }
    }

    @GetMapping("/my-rated-menu-items")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<List<String>>> getMyRatedMenuItems() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UserPrincipal userPrincipal = (UserPrincipal) auth.getPrincipal();
            Long studentId = userPrincipal.getId();

            List<String> ratedMenuItems = ratingService.getRatedMenuItemsByStudent(studentId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Rated menu items retrieved successfully", ratedMenuItems));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "An error occurred while fetching rated menu items", null));
        }
    }
}
