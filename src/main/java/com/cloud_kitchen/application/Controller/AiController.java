package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.Service.AiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiService aiService;

    /**
     * Get AI-suggested menu combinations with full menu item details (JSON response)
     */
    @GetMapping("/suggest-combinations-with-items")
    public ResponseEntity<Map<String, Object>> suggestCombinationsWithItems(
            @RequestParam(value = "itemCount", required = false, defaultValue = "3") Integer itemCount) {
        try {
            log.info("Requesting menu combinations with items: itemCount={}", itemCount);
            Map<String, Object> suggestions = aiService.suggestMenuCombinationsWithItems(itemCount);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("Error in suggestCombinationsWithItems: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to generate suggestions",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Get AI meal recommendations with full menu item details (JSON response)
     */
    @PostMapping("/get-recommendations-with-items")
    public ResponseEntity<Map<String, Object>> getMealRecommendationsWithItems(
            @RequestBody(required = false) Map<String, Object> preferences) {
        try {
            log.info("Requesting meal recommendations with items: preferences={}", preferences);
            Map<String, Object> recommendations = aiService.getMealRecommendationsWithItems(preferences);
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            log.error("Error in getMealRecommendationsWithItems: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to generate recommendations",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Health check endpoint for AI service
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("AI Service is UP and running");
    }
}
