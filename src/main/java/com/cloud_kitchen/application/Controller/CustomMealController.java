package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.*;
import com.cloud_kitchen.application.Service.CustomMealService;
import com.cloud_kitchen.application.Service.GeminiAIService;
import com.cloud_kitchen.application.Service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/custom-meals")
@CrossOrigin(origins = "*")
public class CustomMealController {

    @Autowired
    private CustomMealService customMealService;

    @Autowired
    private GeminiAIService geminiAIService;

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * Generate AI meal (Premium feature - requires active subscription)
     */
    @PostMapping("/ai/generate")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> generateAIMeal(@RequestBody AIMealGenerationRequest request) {
        // Check if student has active subscription
        SubscriptionResponse activeSubscription = subscriptionService.getActiveSubscription();
        if (activeSubscription == null) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "Premium Feature",
                    "message", "AI Meal Builder is a premium feature. Please subscribe to Gold Plan to access this feature."
            ));
        }

        AIMealGenerationResponse response = geminiAIService.generateAIMeal(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get smart recommendations (Premium feature)
     */
    @PostMapping("/ai/recommendations")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> getSmartRecommendations(@RequestBody List<Long> currentItemIds) {
        // Check if student has active subscription
        SubscriptionResponse activeSubscription = subscriptionService.getActiveSubscription();
        if (activeSubscription == null) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "Premium Feature",
                    "message", "Smart Recommendations is a premium feature. Please subscribe to Gold Plan."
            ));
        }

        List<AIMealItem> recommendations = geminiAIService.getSmartRecommendations(currentItemIds);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * Analyze meal (Premium feature)
     */
    @PostMapping("/ai/analyze")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> analyzeMeal(@RequestBody Map<String, Object> request) {
        // Check if student has active subscription
        SubscriptionResponse activeSubscription = subscriptionService.getActiveSubscription();
        if (activeSubscription == null) {
            return ResponseEntity.status(403).body(Map.of(
                    "error", "Premium Feature",
                    "message", "Meal Analysis is a premium feature. Please subscribe to Gold Plan."
            ));
        }

        String mealName = (String) request.get("mealName");
        @SuppressWarnings("unchecked")
        List<Long> itemIds = (List<Long>) request.get("itemIds");

        Map<String, Object> analysis = geminiAIService.analyzeMeal(mealName, itemIds);
        return ResponseEntity.ok(analysis);
    }

    /**
     * Create custom meal
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CustomMealResponse> createCustomMeal(@RequestBody CustomMealRequest request) {
        return ResponseEntity.ok(customMealService.createCustomMeal(request));
    }

    /**
     * Get student's custom meals
     */
    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CustomMealResponse>> getMyCustomMeals() {
        return ResponseEntity.ok(customMealService.getStudentCustomMeals());
    }

    /**
     * Get custom meal by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CustomMealResponse> getCustomMealById(@PathVariable Long id) {
        return ResponseEntity.ok(customMealService.getCustomMealById(id));
    }

    /**
     * Delete custom meal
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> deleteCustomMeal(@PathVariable Long id) {
        customMealService.deleteCustomMeal(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Increment times ordered
     */
    @PostMapping("/{id}/order")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> incrementTimesOrdered(@PathVariable Long id) {
        customMealService.incrementTimesOrdered(id);
        return ResponseEntity.ok().build();
    }
}
