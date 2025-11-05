package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.ApiResponse;
import com.cloud_kitchen.application.DTO.ChefRatingStats;
import com.cloud_kitchen.application.DTO.MenuItemResponse;
import com.cloud_kitchen.application.Entity.User;
import com.cloud_kitchen.application.Service.AuthService;
import com.cloud_kitchen.application.Service.MenuItemService;
import com.cloud_kitchen.application.Service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chef")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CHEF')")
@CrossOrigin(origins = "http://localhost:5173")
public class ChefController {

    private final MenuItemService menuItemService;
    private final AuthService authService;
    private final RatingService ratingService;

    @GetMapping("/my-menu-items")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMyMenuItems() {
        try {
            Long chefId = authService.getCurrentUser().getId();
            List<MenuItemResponse> items = menuItemService.getMenuItemsByChef(chefId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Menu items fetched successfully", items));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/my-ratings")
    public ResponseEntity<ApiResponse<ChefRatingStats>> getMyRatings() {
        try {
            User currentUser = authService.getCurrentUser();
            System.out.println("Current user: " + currentUser.getName() + " (ID: " + currentUser.getId() + ", Role: " + currentUser.getRole() + ")");
            
            Long chefId = currentUser.getId();
            ChefRatingStats stats = ratingService.getChefRatings(chefId);
            System.out.println("Ratings fetched successfully for chef ID: " + chefId);
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Ratings fetched successfully", stats));
        } catch (RuntimeException e) {
            System.err.println("Runtime error in getMyRatings: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(400)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            System.err.println("Unexpected error in getMyRatings: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>(false, "An error occurred while fetching ratings", null));
        }
    }
}