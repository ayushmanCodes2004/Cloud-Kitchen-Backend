package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.ApiResponse;
import com.cloud_kitchen.application.DTO.MenuItemRequest;
import com.cloud_kitchen.application.DTO.MenuItemResponse;
import com.cloud_kitchen.application.DTO.RatingResponse;
import com.cloud_kitchen.application.Service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"api/menu", "/menu"})
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> createMenuItem(@Valid @RequestBody MenuItemRequest request) {
        try {
            MenuItemResponse response = menuItemService.createMenuItem(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<MenuItemResponse>(true, "Menu item created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<MenuItemResponse>(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(@PathVariable Long id,
                                                         @Valid @RequestBody MenuItemRequest request) {
        try {
            MenuItemResponse response = menuItemService.updateMenuItem(id, request);
            return ResponseEntity.ok(new ApiResponse<MenuItemResponse>(true, "Menu item updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<MenuItemResponse>(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<ApiResponse<String>> deleteMenuItem(@PathVariable Long id) {
        try {
            menuItemService.deleteMenuItem(id);
            return ResponseEntity.ok(new ApiResponse<String>(true, "Menu item deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<String>(false, e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getAllMenuItems() {
        try {
            List<MenuItemResponse> items = menuItemService.getAllMenuItems();
            return ResponseEntity.ok(new ApiResponse<List<MenuItemResponse>>(true, "Menu items fetched successfully", items));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<List<MenuItemResponse>>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getAvailableMenuItems() {
        try {
            List<MenuItemResponse> items = menuItemService.getAvailableMenuItems();
            return ResponseEntity.ok(new ApiResponse<List<MenuItemResponse>>(true, "Available menu items fetched successfully", items));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<List<MenuItemResponse>>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> getMenuItemById(@PathVariable Long id) {
        try {
            MenuItemResponse item = menuItemService.getMenuItemById(id);
            return ResponseEntity.ok(new ApiResponse<MenuItemResponse>(true, "Menu item fetched successfully", item));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<MenuItemResponse>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenuItemsByCategory(@PathVariable String category) {
        try {
            List<MenuItemResponse> items = menuItemService.getMenuItemsByCategory(category);
            return ResponseEntity.ok(new ApiResponse<List<MenuItemResponse>>(true, "Menu items fetched successfully", items));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<List<MenuItemResponse>>(false, e.getMessage(), null));
        }
    }

    @GetMapping("/chef/{chefId}")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMenuItemsByChef(@PathVariable Long chefId) {
        try {
            List<MenuItemResponse> items = menuItemService.getMenuItemsByChef(chefId);
            return ResponseEntity.ok(new ApiResponse<List<MenuItemResponse>>(true, "Menu items fetched successfully", items));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<List<MenuItemResponse>>(false, e.getMessage(), null));
        }
    }

    @PatchMapping("/{id}/toggle-availability")
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> toggleAvailability(@PathVariable Long id) {
        try {
            MenuItemResponse response = menuItemService.toggleAvailability(id);
            return ResponseEntity.ok(new ApiResponse<MenuItemResponse>(true, "Availability toggled successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<MenuItemResponse>(false, e.getMessage(), null));
        }
    }
}

