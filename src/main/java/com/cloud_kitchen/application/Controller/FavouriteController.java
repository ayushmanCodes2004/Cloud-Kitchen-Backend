package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.ApiResponse;
import com.cloud_kitchen.application.Entity.Favourite;
import com.cloud_kitchen.application.Entity.User;
import com.cloud_kitchen.application.Repository.UserRepository;
import com.cloud_kitchen.application.Service.FavouriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favourites")
@PreAuthorize("hasRole('STUDENT')")
@Slf4j
public class FavouriteController {

    @Autowired
    private FavouriteService favouriteService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Favourite>>> getMyFavourites() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<Favourite> favourites = favouriteService.getStudentFavourites(user.getId());
            return ResponseEntity.ok(new ApiResponse<>(true, "Favourites retrieved successfully", favourites));
        } catch (Exception e) {
            log.error("Error getting favourites: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/ids")
    public ResponseEntity<ApiResponse<List<Long>>> getFavouriteIds() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            List<Long> ids = favouriteService.getFavouriteMenuItemIds(user.getId());
            return ResponseEntity.ok(new ApiResponse<>(true, "Favourite IDs retrieved successfully", ids));
        } catch (Exception e) {
            log.error("Error getting favourite IDs: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @PostMapping("/{menuItemId}")
    public ResponseEntity<ApiResponse<Favourite>> addFavourite(@PathVariable Long menuItemId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            log.info("POST /api/favourites/{} - User: {} (ID: {})", menuItemId, user.getEmail(), user.getId());
            Favourite favourite = favouriteService.addFavourite(user.getId(), menuItemId);
            log.info("Successfully added favourite");
            return ResponseEntity.ok(new ApiResponse<>(true, "Added to favourites", favourite));
        } catch (Exception e) {
            log.error("Error adding favourite: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{menuItemId}")
    public ResponseEntity<ApiResponse<Void>> removeFavourite(@PathVariable Long menuItemId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            favouriteService.removeFavourite(user.getId(), menuItemId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Removed from favourites", null));
        } catch (Exception e) {
            log.error("Error removing favourite: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/check/{menuItemId}")
    public ResponseEntity<ApiResponse<Boolean>> checkFavourite(@PathVariable Long menuItemId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            boolean isFavourite = favouriteService.isFavourite(user.getId(), menuItemId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Checked favourite status", isFavourite));
        } catch (Exception e) {
            log.error("Error checking favourite: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}
