package com.cloud_kitchen.application.Service;

import com.cloud_kitchen.application.DTO.*;
import com.cloud_kitchen.application.Entity.*;
import com.cloud_kitchen.application.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomMealService {

    @Autowired
    private CustomMealRepository customMealRepository;

    @Autowired
    private CustomMealItemRepository customMealItemRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private ChefRepository chefRepository;

    /**
     * Create custom meal
     */
    @Transactional
    public CustomMealResponse createCustomMeal(CustomMealRequest request) {
        // Get authenticated student
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Calculate total price
        double totalPrice = 0.0;
        for (CustomMealItemRequest itemRequest : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemRequest.getMenuItemId()));
            totalPrice += menuItem.getPrice() * itemRequest.getQuantity();
        }

        // Create custom meal
        CustomMeal customMeal = new CustomMeal();
        customMeal.setStudent(student);
        customMeal.setName(request.getName());
        customMeal.setDescription(request.getDescription());
        customMeal.setTotalPrice(totalPrice);
        customMeal.setAiGenerated(request.getAiGenerated() != null ? request.getAiGenerated() : false);
        customMeal.setAiPrompt(request.getAiPrompt());

        customMeal = customMealRepository.save(customMeal);

        // Create custom meal items
        List<CustomMealItem> customMealItems = new ArrayList<>();
        for (CustomMealItemRequest itemRequest : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));

            CustomMealItem customMealItem = new CustomMealItem();
            customMealItem.setCustomMeal(customMeal);
            customMealItem.setMenuItem(menuItem);
            customMealItem.setQuantity(itemRequest.getQuantity());
            customMealItem.setChef(menuItem.getChef());
            customMealItem.setAiReason(itemRequest.getAiReason());

            customMealItems.add(customMealItemRepository.save(customMealItem));
        }

        customMeal.setCustomMealItems(customMealItems);

        return convertToResponse(customMeal);
    }

    /**
     * Get student's custom meals
     */
    @Transactional(readOnly = true)
    public List<CustomMealResponse> getStudentCustomMeals() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<CustomMeal> customMeals = customMealRepository.findByStudentIdOrderByCreatedAtDesc(student.getId());
        return customMeals.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get custom meal by ID
     */
    @Transactional(readOnly = true)
    public CustomMealResponse getCustomMealById(Long id) {
        CustomMeal customMeal = customMealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Custom meal not found"));

        // Verify ownership
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!customMeal.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Unauthorized access to custom meal");
        }

        return convertToResponse(customMeal);
    }

    /**
     * Delete custom meal
     */
    @Transactional
    public void deleteCustomMeal(Long id) {
        CustomMeal customMeal = customMealRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Custom meal not found"));

        // Verify ownership
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!customMeal.getStudent().getId().equals(student.getId())) {
            throw new RuntimeException("Unauthorized access to custom meal");
        }

        customMealRepository.delete(customMeal);
    }

    /**
     * Increment times ordered
     */
    @Transactional
    public void incrementTimesOrdered(Long customMealId) {
        CustomMeal customMeal = customMealRepository.findById(customMealId)
                .orElseThrow(() -> new RuntimeException("Custom meal not found"));

        customMeal.setTimesOrdered(customMeal.getTimesOrdered() + 1);
        customMealRepository.save(customMeal);
    }

    /**
     * Convert CustomMeal to CustomMealResponse
     */
    private CustomMealResponse convertToResponse(CustomMeal customMeal) {
        CustomMealResponse response = new CustomMealResponse();
        response.setId(customMeal.getId());
        response.setStudentId(customMeal.getStudent().getId());
        response.setName(customMeal.getName());
        response.setDescription(customMeal.getDescription());
        response.setTotalPrice(customMeal.getTotalPrice());
        response.setAiGenerated(customMeal.getAiGenerated());
        response.setAiPrompt(customMeal.getAiPrompt());
        response.setNutritionalScore(customMeal.getNutritionalScore());
        response.setTimesOrdered(customMeal.getTimesOrdered());
        response.setCreatedAt(customMeal.getCreatedAt());
        response.setUpdatedAt(customMeal.getUpdatedAt());

        // Convert items
        List<CustomMealItemResponse> itemResponses = customMeal.getCustomMealItems().stream()
                .map(this::convertItemToResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);

        return response;
    }

    /**
     * Convert CustomMealItem to CustomMealItemResponse
     */
    private CustomMealItemResponse convertItemToResponse(CustomMealItem item) {
        CustomMealItemResponse response = new CustomMealItemResponse();
        response.setId(item.getId());
        response.setMenuItemId(item.getMenuItem().getId());
        response.setMenuItemName(item.getMenuItem().getName());
        response.setMenuItemPrice(item.getMenuItem().getPrice());
        response.setMenuItemImage(item.getMenuItem().getImageUrl());
        response.setQuantity(item.getQuantity());
        response.setChefId(item.getChef().getId());
        response.setChefName(item.getChef().getName());
        response.setAiReason(item.getAiReason());
        return response;
    }
}
