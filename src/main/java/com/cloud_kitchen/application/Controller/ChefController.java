package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.ApiResponse;
import com.cloud_kitchen.application.DTO.MenuItemResponse;
import com.cloud_kitchen.application.Service.AuthService;
import com.cloud_kitchen.application.Service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/chef")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CHEF')")
public class ChefController {

    private final MenuItemService menuItemService;
    private final AuthService authService;

    @GetMapping("/my-menu-items")
    public ResponseEntity<ApiResponse> getMyMenuItems() {
        try {
            Long chefId = authService.getCurrentUser().getId();
            List<MenuItemResponse> items = menuItemService.getMenuItemsByChef(chefId);
            return ResponseEntity.ok(new ApiResponse(true, "Menu items fetched successfully", items));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}