package com.cloud_kitchen.application.Service;

import com.cloud_kitchen.application.DTO.MenuItemRequest;
import com.cloud_kitchen.application.DTO.MenuItemResponse;
import com.cloud_kitchen.application.Entity.Chef;
import com.cloud_kitchen.application.Entity.MenuItem;
import com.cloud_kitchen.application.Entity.User;
import com.cloud_kitchen.application.Repository.ChefRepository;
import com.cloud_kitchen.application.Repository.MenuItemRepository;
import com.cloud_kitchen.application.Repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final ChefRepository chefRepository;
    private final AuthService authService;
    private final RatingRepository ratingRepository;

    @Transactional
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        User currentUser = authService.getCurrentUser();

        Chef chef = chefRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        MenuItem menuItem = new MenuItem();
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(request.getCategory());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setVegetarian(request.getVegetarian() != null ? request.getVegetarian() : false);
        menuItem.setPreparationTime(request.getPreparationTime());
        menuItem.setAvailable(true);
        menuItem.setChef(chef);

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        return convertToResponse(savedMenuItem);
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        User currentUser = authService.getCurrentUser();
        if (!menuItem.getChef().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only update your own menu items");
        }

        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(request.getCategory());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setVegetarian(request.getVegetarian() != null ? request.getVegetarian() : false);
        menuItem.setPreparationTime(request.getPreparationTime());

        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        return convertToResponse(updatedMenuItem);
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        User currentUser = authService.getCurrentUser();
        if (!menuItem.getChef().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only delete your own menu items");
        }

        menuItemRepository.delete(menuItem);
    }

    public List<MenuItemResponse> getAllMenuItems() {
        return menuItemRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getAvailableMenuItems() {
        return menuItemRepository.findByAvailable(true).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public MenuItemResponse getMenuItemById(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        return convertToResponse(menuItem);
    }

    public List<MenuItemResponse> getMenuItemsByCategory(String category) {
        return menuItemRepository.findByCategory(category).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItemsByChef(Long chefId) {
        Chef chef = chefRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef not found"));
        return menuItemRepository.findByChef(chef).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuItemResponse toggleAvailability(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        User currentUser = authService.getCurrentUser();
        if (!menuItem.getChef().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only toggle availability of your own menu items");
        }

        menuItem.setAvailable(!menuItem.getAvailable());
        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        return convertToResponse(updatedMenuItem);
    }

    private MenuItemResponse convertToResponse(MenuItem menuItem) {
        MenuItemResponse response = new MenuItemResponse();
        response.setId(menuItem.getId());
        response.setName(menuItem.getName());
        response.setDescription(menuItem.getDescription());
        response.setPrice(menuItem.getPrice());
        response.setCategory(menuItem.getCategory());
        response.setImageUrl(menuItem.getImageUrl());
        response.setAvailable(menuItem.getAvailable());
        response.setVegetarian(menuItem.getVegetarian());
        response.setPreparationTime(menuItem.getPreparationTime());
        response.setChefName(menuItem.getChef().getName());
        response.setChefId(menuItem.getChef().getId());
        response.setChefVerified(menuItem.getChef().getVerified() != null ? menuItem.getChef().getVerified() : false);
        
        // Add chef ratings
        Double chefAvgRating = ratingRepository.findAverageRatingByChefId(menuItem.getChef().getId());
        Long chefTotalRatings = ratingRepository.countRatingsByChefId(menuItem.getChef().getId());
        response.setChefAverageRating(chefAvgRating != null ? chefAvgRating : 0.0);
        response.setChefTotalRatings(chefTotalRatings != null ? chefTotalRatings : 0L);
        
        // Add menu item ratings
        Double menuItemAvgRating = ratingRepository.findAverageRatingByMenuItemId(menuItem.getId());
        Long menuItemTotalRatings = ratingRepository.countRatingsByMenuItemId(menuItem.getId());
        response.setMenuItemAverageRating(menuItemAvgRating != null ? menuItemAvgRating : 0.0);
        response.setMenuItemTotalRatings(menuItemTotalRatings != null ? menuItemTotalRatings : 0L);
        
        return response;
    }
}
