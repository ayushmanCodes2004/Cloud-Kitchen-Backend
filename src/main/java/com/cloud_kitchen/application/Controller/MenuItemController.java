package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.ApiResponse;
import com.cloud_kitchen.application.DTO.MenuItemRequest;
import com.cloud_kitchen.application.DTO.MenuItemResponse;
import com.cloud_kitchen.application.Service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<ApiResponse> createMenuItem(@Valid @RequestBody MenuItemRequest request) {
        try {
            MenuItemResponse response = menuItemService.createMenuItem(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Menu item created successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<ApiResponse> updateMenuItem(@PathVariable Long id,
                                                      @Valid @RequestBody MenuItemRequest request) {
        try {
            MenuItemResponse response = menuItemService.updateMenuItem(id, request);
            return ResponseEntity.ok(new ApiResponse(true, "Menu item updated successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<ApiResponse> deleteMenuItem(@PathVariable Long id) {
        try {
            menuItemService.deleteMenuItem(id);
            return ResponseEntity.ok(new ApiResponse(true, "Menu item deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllMenuItems() {
        try {
            List<MenuItemResponse> items = menuItemService.getAllMenuItems();
            return ResponseEntity.ok(new ApiResponse(true, "Menu items fetched successfully", items));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse> getAvailableMenuItems() {
        try {
            List<MenuItemResponse> items = menuItemService.getAvailableMenuItems();
            return ResponseEntity.ok(new ApiResponse(true, "Available menu items fetched successfully", items));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getMenuItemById(@PathVariable Long id) {
        try {
            MenuItemResponse item = menuItemService.getMenuItemById(id);
            return ResponseEntity.ok(new ApiResponse(true, "Menu item fetched successfully", item));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse> getMenuItemsByCategory(@PathVariable String category) {
        try {
            List<MenuItemResponse> items = menuItemService.getMenuItemsByCategory(category);
            return ResponseEntity.ok(new ApiResponse(true, "Menu items fetched successfully", items));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/chef/{chefId}")
    public ResponseEntity<ApiResponse> getMenuItemsByChef(@PathVariable Long chefId) {
        try {
            List<MenuItemResponse> items = menuItemService.getMenuItemsByChef(chefId);
            return ResponseEntity.ok(new ApiResponse(true, "Menu items fetched successfully", items));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PatchMapping("/{id}/toggle-availability")
    @PreAuthorize("hasRole('CHEF')")
    public ResponseEntity<ApiResponse> toggleAvailability(@PathVariable Long id) {
        try {
            MenuItemResponse response = menuItemService.toggleAvailability(id);
            return ResponseEntity.ok(new ApiResponse(true, "Availability toggled successfully", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}

