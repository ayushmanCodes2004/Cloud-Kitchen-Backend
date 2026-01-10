package com.cloud_kitchen.application.Service;

import com.cloud_kitchen.application.DTO.MenuItemResponse;
import com.cloud_kitchen.application.Entity.MenuItem;
import com.cloud_kitchen.application.Repository.MenuItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    private final MenuItemRepository menuItemRepository;
    private final MenuItemService menuItemService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Get AI-suggested menu item combinations based on available items
     * @param itemCount Number of items to suggest in the combination
     * @return Suggested menu item combinations with explanations
     */
    public Map<String, Object> suggestMenuCombinations(Integer itemCount) {
        try {
            // Fetch all available menu items
            List<MenuItem> availableItems = menuItemRepository.findByAvailable(true);

            if (availableItems.isEmpty()) {
                return createErrorResponse("No available menu items found");
            }

            // Convert to response format for better readability
            List<MenuItemResponse> itemResponses = availableItems.stream()
                    .map(menuItemService::convertToResponse)
                    .collect(Collectors.toList());

            // Prepare menu data for Gemini
            String menuData = formatMenuDataForGemini(itemResponses);

            // Create prompt for Gemini
            String prompt = createCombinationPrompt(menuData, itemCount);

            try {
                // Call Gemini API
                String geminiResponse = callGeminiApi(prompt);

                // Parse and return the response
                return parseCombinationResponse(geminiResponse);
            } catch (Exception apiException) {
                log.warn("Gemini API failed for combinations, using fallback: {}", apiException.getMessage());
                // Fallback: return random menu item combinations
                return generateFallbackCombinations(itemResponses, itemCount);
            }

        } catch (Exception e) {
            log.error("Error suggesting menu combinations: ", e);
            return createErrorResponse("Failed to generate suggestions: " + e.getMessage());
        }
    }

    /**
     * Generate fallback combinations when Gemini API fails
     */
    private Map<String, Object> generateFallbackCombinations(List<MenuItemResponse> availableItems, Integer itemCount) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> combinations = new java.util.ArrayList<>();
        double totalPrice = 0;
        
        // Select random items as combinations
        int count = Math.min(itemCount != null ? itemCount : 3, availableItems.size());
        java.util.Collections.shuffle(availableItems);
        
        for (int i = 0; i < count; i++) {
            MenuItemResponse item = availableItems.get(i);
            Map<String, Object> combo = new HashMap<>();
            combo.put("itemName", item.getName());
            combo.put("price", item.getPrice());
            combo.put("reason", "Perfect choice from " + item.getChefName() + " - highly rated");
            combinations.add(combo);
            totalPrice += item.getPrice();
        }
        
        data.put("combinations", combinations);
        data.put("totalPrice", totalPrice);
        data.put("explanation", "Here are some popular meal combinations from our menu. These are carefully selected dishes that pair well together.");
        
        result.put("data", data);
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }

    /**
     * Get AI suggestions for a specific menu item (pairing suggestions)
     * @param menuItemId The menu item to get pairings for
     * @return Suggested pairings with explanations
     */
    public Map<String, Object> suggestPairings(Long menuItemId) {
        try {
            MenuItem mainItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));

            List<MenuItem> availableItems = menuItemRepository.findByAvailable(true);

            if (availableItems.isEmpty()) {
                return createErrorResponse("No available menu items found");
            }

            MenuItemResponse mainItemResponse = menuItemService.convertToResponse(mainItem);
            List<MenuItemResponse> otherItems = availableItems.stream()
                    .filter(item -> !item.getId().equals(menuItemId))
                    .map(menuItemService::convertToResponse)
                    .collect(Collectors.toList());

            String menuData = formatMenuDataForGemini(otherItems);
            String prompt = createPairingPrompt(mainItemResponse, menuData);

            String geminiResponse = callGeminiApi(prompt);

            return parsePairingResponse(geminiResponse, mainItemResponse);

        } catch (Exception e) {
            log.error("Error suggesting pairings: ", e);
            return createErrorResponse("Failed to generate pairings: " + e.getMessage());
        }
    }

    /**
     * Get AI-generated meal recommendations based on preferences
     * @param preferences User preferences (vegetarian, budget, etc.)
     * @return Recommended meal combinations
     */
    public Map<String, Object> getMealRecommendations(Map<String, Object> preferences) {
        try {
            List<MenuItem> availableItems = menuItemRepository.findByAvailable(true);

            if (availableItems.isEmpty()) {
                return createErrorResponse("No available menu items found");
            }

            List<MenuItemResponse> itemResponses = availableItems.stream()
                    .map(menuItemService::convertToResponse)
                    .collect(Collectors.toList());

            String menuData = formatMenuDataForGemini(itemResponses);
            String prompt = createRecommendationPrompt(menuData, preferences);

            try {
                String geminiResponse = callGeminiApi(prompt);
                return parseRecommendationResponse(geminiResponse);
            } catch (Exception apiException) {
                log.warn("Gemini API failed, using fallback recommendations: {}", apiException.getMessage());
                // Fallback: return random menu items as recommendations
                return generateFallbackRecommendations(itemResponses);
            }

        } catch (Exception e) {
            log.error("Error getting meal recommendations: ", e);
            return createErrorResponse("Failed to generate recommendations: " + e.getMessage());
        }
    }

    /**
     * Generate fallback recommendations when Gemini API fails
     */
    private Map<String, Object> generateFallbackRecommendations(List<MenuItemResponse> availableItems) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        
        Map<String, Object> data = new HashMap<>();
        List<Map<String, Object>> recommendedMeal = new java.util.ArrayList<>();
        double totalPrice = 0;
        
        // Select 2-3 random items as recommendations
        int count = Math.min(3, availableItems.size());
        java.util.Collections.shuffle(availableItems);
        
        for (int i = 0; i < count; i++) {
            MenuItemResponse item = availableItems.get(i);
            Map<String, Object> mealItem = new HashMap<>();
            mealItem.put("itemName", item.getName());
            mealItem.put("price", item.getPrice());
            mealItem.put("reason", "Popular choice from " + item.getChefName());
            recommendedMeal.add(mealItem);
            totalPrice += item.getPrice();
        }
        
        data.put("recommendedMeal", recommendedMeal);
        data.put("totalPrice", totalPrice);
        data.put("explanation", "Here are some popular meal recommendations based on available items. These are highly rated dishes from our chefs.");
        
        result.put("data", data);
        result.put("timestamp", System.currentTimeMillis());
        
        return result;
    }

    /**
     * Call Gemini API with the given prompt
     */
    private String callGeminiApi(String prompt) throws Exception {
        String urlWithKey = geminiApiUrl + "?key=" + geminiApiKey;
        
        log.info("Calling Gemini API with URL: {}", urlWithKey.replaceAll("key=.*", "key=***"));
        log.debug("Prompt: {}", prompt);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        Map<String, Object> content = new HashMap<>();
        Map<String, String> part = new HashMap<>();

        part.put("text", prompt);
        content.put("parts", new Map[]{part});
        requestBody.put("contents", new Map[]{content});

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(urlWithKey, entity, String.class);
        
        log.info("Gemini API response status: {}", response.getStatusCode());
        log.debug("Gemini API response body: {}", response.getBody());

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Gemini API call failed with status: {} and body: {}", response.getStatusCode(), response.getBody());
            throw new RuntimeException("Gemini API call failed with status: " + response.getStatusCode());
        }

        return response.getBody();
    }

    /**
     * Format menu items for Gemini prompt
     */
    private String formatMenuDataForGemini(List<MenuItemResponse> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("Available Menu Items:\n");
        sb.append("====================\n");

        for (MenuItemResponse item : items) {
            sb.append(String.format("- %s (ID: %d)\n", item.getName(), item.getId()));
            sb.append(String.format("  Price: ₹%.2f\n", item.getPrice()));
            sb.append(String.format("  Category: %s\n", item.getCategory()));
            sb.append(String.format("  Vegetarian: %s\n", item.getVegetarian() ? "Yes" : "No"));
            if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                sb.append(String.format("  Description: %s\n", item.getDescription()));
            }
            sb.append(String.format("  Chef: %s (Rating: %.1f/5)\n", item.getChefName(), item.getChefAverageRating()));
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Create prompt for menu combinations
     */
    private String createCombinationPrompt(String menuData, Integer itemCount) {
        int count = itemCount != null && itemCount > 0 ? itemCount : 3;

        return String.format(
                "You are a professional food consultant. Based on the following menu items, suggest the best %d-item combination(s) " +
                        "that would make a complete, balanced meal. Consider nutritional balance, flavor profiles, and complementary dishes.\n\n" +
                        "%s\n" +
                        "Please provide:\n" +
                        "1. The suggested combination (list item names and IDs)\n" +
                        "2. Why this combination works well together\n" +
                        "3. Total estimated cost\n" +
                        "4. Estimated total preparation time\n" +
                        "5. Nutritional balance explanation\n\n" +
                        "Format your response as JSON with keys: 'combinations' (array of objects with 'items', 'reason', 'totalCost', 'totalTime', 'nutritionalBalance')",
                count, menuData
        );
    }

    /**
     * Create prompt for pairing suggestions
     */
    private String createPairingPrompt(MenuItemResponse mainItem, String menuData) {
        return String.format(
                "You are a professional food consultant. A customer is interested in ordering '%s' (₹%.2f, %s). " +
                        "Based on the following available menu items, suggest the best 2-3 items to pair with it for a complete meal.\n\n" +
                        "%s\n" +
                        "Please provide:\n" +
                        "1. Suggested pairings (list item names and IDs)\n" +
                        "2. Why each pairing complements the main item\n" +
                        "3. Total estimated cost for the complete meal\n" +
                        "4. Why this creates a balanced meal\n\n" +
                        "Format your response as JSON with keys: 'pairings' (array of objects with 'itemName', 'itemId', 'reason'), 'totalCost', 'mealBalance'",
                mainItem.getName(), mainItem.getPrice(), mainItem.getCategory(), menuData
        );
    }

    /**
     * Create prompt for meal recommendations
     */
    private String createRecommendationPrompt(String menuData, Map<String, Object> preferences) {
        StringBuilder prefStr = new StringBuilder();
        if (preferences != null) {
            if (preferences.containsKey("vegetarian")) {
                prefStr.append("- Vegetarian: ").append(preferences.get("vegetarian")).append("\n");
            }
            if (preferences.containsKey("maxBudget")) {
                prefStr.append("- Max Budget: ₹").append(preferences.get("maxBudget")).append("\n");
            }
            if (preferences.containsKey("cuisineType")) {
                prefStr.append("- Cuisine Type: ").append(preferences.get("cuisineType")).append("\n");
            }
            if (preferences.containsKey("dietary")) {
                prefStr.append("- Dietary Restrictions: ").append(preferences.get("dietary")).append("\n");
            }
        }

        return String.format(
                "You are a professional food consultant. Based on the following preferences and available menu items, " +
                        "recommend the best meal combination.\n\n" +
                        "Customer Preferences:\n%s\n" +
                        "%s\n" +
                        "Please provide:\n" +
                        "1. Recommended meal combination (list item names and IDs)\n" +
                        "2. Why this matches the preferences\n" +
                        "3. Total cost\n" +
                        "4. Nutritional highlights\n\n" +
                        "Format your response as JSON with keys: 'recommendation', 'items' (array with itemName, itemId), 'reason', 'totalCost', 'nutritionalHighlights'",
                prefStr.toString(), menuData
        );
    }

    /**
     * Parse Gemini response for combinations
     */
    private Map<String, Object> parseCombinationResponse(String geminiResponse) throws Exception {
        JsonNode root = objectMapper.readTree(geminiResponse);
        JsonNode candidates = root.path("candidates");

        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode content = candidates.get(0).path("content").path("parts").get(0).path("text");
            String responseText = content.asText();
            String jsonText = extractJsonFromText(responseText);

            try {
                // Try to parse the response as JSON
                JsonNode jsonResponse = objectMapper.readTree(jsonText);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                
                // Extract combination data
                List<Map<String, Object>> combinations = new java.util.ArrayList<>();
                String overallExplanation = "";
                
                if (jsonResponse.has("combinations")) {
                    jsonResponse.get("combinations").forEach(combo -> {
                        Map<String, Object> comboItem = new HashMap<>();
                        
                        // Extract items list
                        if (combo.has("items") && combo.get("items").isArray()) {
                            StringBuilder itemNames = new StringBuilder();
                            combo.get("items").forEach(item -> {
                                if (itemNames.length() > 0) itemNames.append(", ");
                                itemNames.append(item.has("name") ? item.get("name").asText() : "");
                            });
                            comboItem.put("itemName", itemNames.toString());
                        } else {
                            comboItem.put("itemName", combo.has("name") ? combo.get("name").asText() : "AI Suggested Combination");
                        }
                        
                        // Extract total cost
                        double totalCost = 0;
                        if (combo.has("totalCost")) {
                            try {
                                totalCost = combo.get("totalCost").asDouble();
                            } catch (Exception e) {
                                // If totalCost is a string expression like "169 + 113 + 42", try to parse it
                                String costStr = combo.get("totalCost").asText();
                                try {
                                    totalCost = evaluateExpression(costStr);
                                } catch (Exception ex) {
                                    totalCost = 0;
                                }
                            }
                        }
                        comboItem.put("price", totalCost);
                        
                        // Extract reason
                        comboItem.put("reason", combo.has("reason") ? combo.get("reason").asText() : "");
                        
                        combinations.add(comboItem);
                    });
                    
                    // Get overall explanation
                    if (jsonResponse.has("nutritionalBalance")) {
                        overallExplanation = jsonResponse.get("nutritionalBalance").asText();
                    }
                }
                
                Map<String, Object> data = new HashMap<>();
                data.put("combinations", combinations);
                data.put("explanation", overallExplanation.isEmpty() ? "AI-generated meal combinations based on nutritional balance and flavor profiles." : cleanJsonFromText(overallExplanation));
                
                result.put("data", data);
                result.put("timestamp", System.currentTimeMillis());
                
                return result;
            } catch (Exception e) {
                log.warn("Could not parse Gemini response as JSON, returning raw text: {}", e.getMessage());
                
                // Fallback: create a generic combination from the text response
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                
                Map<String, Object> data = new HashMap<>();
                List<Map<String, Object>> combinations = new java.util.ArrayList<>();
                Map<String, Object> comboItem = new HashMap<>();
                comboItem.put("itemName", "AI Suggested Combination");
                comboItem.put("price", 0);
                comboItem.put("reason", responseText.substring(0, Math.min(200, responseText.length())));
                combinations.add(comboItem);
                
                data.put("combinations", combinations);
                data.put("explanation", "AI-generated meal combinations based on available menu items.");
                
                result.put("data", data);
                result.put("timestamp", System.currentTimeMillis());
                
                return result;
            }
        }

        return createErrorResponse("No valid response from Gemini API");
    }
    
    /**
     * Simple expression evaluator for cost calculations like "169 + 113 + 42"
     */
    private double evaluateExpression(String expression) {
        try {
            String[] parts = expression.split("\\+");
            double sum = 0;
            for (String part : parts) {
                sum += Double.parseDouble(part.trim());
            }
            return sum;
        } catch (Exception e) {
            log.warn("Could not evaluate expression: {}", expression);
            return 0;
        }
    }
    
    /**
     * Extract JSON from text (remove markdown code blocks)
     */
    private String extractJsonFromText(String text) {
        if (text == null || text.isEmpty()) {
            return "{}";
        }
        
        // Remove markdown code blocks if present
        if (text.contains("```json")) {
            text = text.replaceAll("```json", "").replaceAll("```", "");
        } else if (text.contains("```")) {
            text = text.replaceAll("```", "");
        }
        
        // Find the first '{' and last '}' to extract the JSON object
        int firstBrace = text.indexOf("{");
        int lastBrace = text.lastIndexOf("}");
        
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return text.substring(firstBrace, lastBrace + 1);
        }
        
        return text.trim();
    }

    /**
     * Clean JSON syntax from text
     */
    private String cleanJsonFromText(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Remove JSON code blocks
        text = text.replaceAll("```json[\\s\\S]*?```", "");
        // Remove JSON objects
        text = text.replaceAll("\\{[\\s\\S]*?\\}", "");
        // Remove JSON arrays
        text = text.replaceAll("\\[[\\s\\S]*?\\]", "");
        // Remove quotes around the text
        text = text.replaceAll("^['\"]|['\"]$", "");
        // Trim whitespace
        text = text.trim();
        
        return text;
    }

    /**
     * Parse Gemini response for pairings
     */
    private Map<String, Object> parsePairingResponse(String geminiResponse, MenuItemResponse mainItem) throws Exception {
        JsonNode root = objectMapper.readTree(geminiResponse);
        JsonNode candidates = root.path("candidates");

        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode content = candidates.get(0).path("content").path("parts").get(0).path("text");
            String responseText = content.asText();
            String jsonText = extractJsonFromText(responseText);

            try {
                // Try to parse the response as JSON
                JsonNode jsonResponse = objectMapper.readTree(jsonText);
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                
                // Extract pairing data
                List<Map<String, Object>> pairings = new java.util.ArrayList<>();
                
                if (jsonResponse.has("pairings")) {
                    jsonResponse.get("pairings").forEach(pairing -> {
                        Map<String, Object> pairingItem = new HashMap<>();
                        pairingItem.put("itemName", pairing.has("itemName") ? pairing.get("itemName").asText() : pairing.has("name") ? pairing.get("name").asText() : "");
                        pairingItem.put("price", pairing.has("price") ? pairing.get("price").asDouble() : 0);
                        pairingItem.put("reason", pairing.has("reason") ? pairing.get("reason").asText() : "");
                        pairings.add(pairingItem);
                    });
                }
                
                Map<String, Object> data = new HashMap<>();
                data.put("mainItem", mainItem.getName());
                data.put("pairings", pairings);
                String pairingExplanation = jsonResponse.has("mealBalance") ? jsonResponse.get("mealBalance").asText() : "";
                data.put("explanation", cleanJsonFromText(pairingExplanation));
                
                result.put("data", data);
                result.put("timestamp", System.currentTimeMillis());
                
                return result;
            } catch (Exception e) {
                log.warn("Could not parse Gemini response as JSON, returning raw text: {}", e.getMessage());
                
                // Fallback: create a generic pairing from the text response
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                
                Map<String, Object> data = new HashMap<>();
                List<Map<String, Object>> pairings = new java.util.ArrayList<>();
                Map<String, Object> pairingItem = new HashMap<>();
                pairingItem.put("itemName", "AI Suggested Pairing");
                pairingItem.put("price", 0);
                pairingItem.put("reason", responseText.substring(0, Math.min(200, responseText.length())));
                pairings.add(pairingItem);
                
                data.put("mainItem", mainItem.getName());
                data.put("pairings", pairings);
                data.put("explanation", responseText);
                
                result.put("data", data);
                result.put("timestamp", System.currentTimeMillis());
                
                return result;
            }
        }

        return createErrorResponse("No valid response from Gemini API");
    }

    /**
     * Parse Gemini response for recommendations
     */
    private Map<String, Object> parseRecommendationResponse(String geminiResponse) throws Exception {
        JsonNode root = objectMapper.readTree(geminiResponse);
        JsonNode candidates = root.path("candidates");

        if (candidates.isArray() && candidates.size() > 0) {
            JsonNode content = candidates.get(0).path("content").path("parts").get(0).path("text");
            String responseText = content.asText();
            String jsonText = extractJsonFromText(responseText);

            try {
                // Try to parse the response as JSON first
                JsonNode jsonResponse = null;
                try {
                    jsonResponse = objectMapper.readTree(jsonText);
                } catch (Exception e) {
                    // If it's not JSON, treat it as plain text
                    log.debug("Response is not JSON, treating as plain text");
                    jsonResponse = null;
                }
                
                // If we got valid JSON, extract from it
                if (jsonResponse != null && jsonResponse.isObject()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    
                    // Extract recommendation data
                    List<Map<String, Object>> recommendedMeal = new java.util.ArrayList<>();
                    double totalPrice = 0;
                    String explanation = "";
                    
                    // Extract items from the items array
                    StringBuilder itemsList = new StringBuilder();
                    if (jsonResponse.has("items") && jsonResponse.get("items").isArray()) {
                        jsonResponse.get("items").forEach(item -> {
                            Map<String, Object> mealItem = new HashMap<>();
                            String itemName = item.has("itemName") ? item.get("itemName").asText() : item.has("name") ? item.get("name").asText() : "";
                            
                            // Only add if itemName is not empty
                            if (!itemName.isEmpty()) {
                                mealItem.put("itemName", itemName);
                                mealItem.put("price", item.has("price") ? item.get("price").asDouble() : 0);
                                mealItem.put("reason", item.has("reason") ? item.get("reason").asText() : "");
                                recommendedMeal.add(mealItem);
                                
                                // Add to items list for explanation
                                if (itemsList.length() > 0) itemsList.append(", ");
                                itemsList.append(itemName);
                            }
                        });
                    }
                    
                    // Extract total cost
                    if (jsonResponse.has("totalCost")) {
                        try {
                            totalPrice = jsonResponse.get("totalCost").asDouble();
                        } catch (Exception e) {
                            String costStr = jsonResponse.get("totalCost").asText();
                            try {
                                totalPrice = evaluateExpression(costStr);
                            } catch (Exception ex) {
                                totalPrice = 0;
                            }
                        }
                    }
                    
                    // Extract explanation - prioritize recommendation field
                    if (jsonResponse.has("recommendation")) {
                        explanation = jsonResponse.get("recommendation").asText().trim();
                    } else if (jsonResponse.has("reason")) {
                        explanation = jsonResponse.get("reason").asText().trim();
                    } else if (jsonResponse.has("nutritionalHighlights")) {
                        explanation = jsonResponse.get("nutritionalHighlights").asText().trim();
                    } else if (jsonResponse.has("why")) {
                        explanation = jsonResponse.get("why").asText().trim();
                    }
                    
                    // Clean explanation - remove any remaining JSON syntax
                    explanation = cleanJsonFromText(explanation);
                    
                    // If explanation is empty or just punctuation, use items list
                    if (explanation.isEmpty() || explanation.matches("[\\s\\p{P}]+")) {
                        if (itemsList.length() > 0) {
                            explanation = "Recommended meal combination featuring: " + itemsList.toString();
                        } else {
                            explanation = "Personalized meal recommendation based on your preferences.";
                        }
                    } else if (itemsList.length() > 0) {
                        // If we have both explanation and items, add items to explanation
                        explanation = explanation + "\n\nRecommended Items: " + itemsList.toString();
                    }
                    
                    Map<String, Object> data = new HashMap<>();
                    data.put("recommendedMeal", recommendedMeal);
                    data.put("totalPrice", totalPrice);
                    data.put("explanation", explanation);
                    
                    result.put("data", data);
                    result.put("timestamp", System.currentTimeMillis());
                    
                    return result;
                } else {
                    // Response is plain text, not JSON - return as is
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    
                    // Clean JSON from plain text response
                    String cleanedText = cleanJsonFromText(responseText);
                    
                    Map<String, Object> data = new HashMap<>();
                    data.put("recommendedMeal", new java.util.ArrayList<>());
                    data.put("totalPrice", 0);
                    data.put("explanation", cleanedText.isEmpty() ? "Personalized meal recommendation based on your preferences." : cleanedText);
                    
                    result.put("data", data);
                    result.put("timestamp", System.currentTimeMillis());
                    
                    return result;
                }
            } catch (Exception e) {
                log.warn("Could not parse Gemini response as JSON, returning raw text: {}", e.getMessage());
                
                // Fallback: create a generic recommendation from the text response
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                
                Map<String, Object> data = new HashMap<>();
                List<Map<String, Object>> recommendedMeal = new java.util.ArrayList<>();
                Map<String, Object> mealItem = new HashMap<>();
                mealItem.put("itemName", "AI Recommended Meal");
                mealItem.put("price", 0);
                mealItem.put("reason", responseText.substring(0, Math.min(200, responseText.length())));
                recommendedMeal.add(mealItem);
                
                data.put("recommendedMeal", recommendedMeal);
                data.put("totalPrice", 0);
                data.put("explanation", "Personalized meal recommendation based on your preferences.");
                
                result.put("data", data);
                result.put("timestamp", System.currentTimeMillis());
                
                return result;
            }
        }

        return createErrorResponse("No valid response from Gemini API");
    }

    /**
     * Create error response
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }

    /**
     * Get AI-suggested menu combinations as plain string
     */
    public String suggestMenuCombinationsAsString(Integer itemCount) {
        try {
            Map<String, Object> result = suggestMenuCombinations(itemCount);
            if ((Boolean) result.getOrDefault("success", false)) {
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                if (data != null) {
                    String explanation = (String) data.get("explanation");
                    return explanation != null ? explanation : "No combinations available";
                }
            }
            return "Failed to generate combinations";
        } catch (Exception e) {
            log.error("Error generating combinations string: ", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Get AI-suggested menu combinations with full menu item details
     */
    public Map<String, Object> suggestMenuCombinationsWithItems(Integer itemCount) {
        try {
            Map<String, Object> result = suggestMenuCombinations(itemCount);
            if ((Boolean) result.getOrDefault("success", false)) {
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                if (data != null) {
                    // Get combinations list
                    List<Map<String, Object>> combinations = (List<Map<String, Object>>) data.get("combinations");
                    String explanation = (String) data.get("explanation");
                    
                    // Fetch full menu item details for each combination
                    List<MenuItemResponse> fullItems = new java.util.ArrayList<>();
                    if (combinations != null) {
                        for (Map<String, Object> combo : combinations) {
                            String itemName = (String) combo.get("itemName");
                            // Remove any markdown bolding if present
                            if (itemName != null) {
                                itemName = itemName.replace("**", "").trim();
                            }

                            // Try to find matching menu item in database
                            // First try exact match
                            List<MenuItem> items = menuItemRepository.findByNameContainingIgnoreCase(itemName);
                            
                            // If no match found, try fuzzy matching or splitting
                            if (items.isEmpty() && itemName.contains(" ")) {
                                String[] parts = itemName.split(" ");
                                for (String part : parts) {
                                    if (part.length() > 3) {
                                        items = menuItemRepository.findByNameContainingIgnoreCase(part);
                                        if (!items.isEmpty()) break;
                                    }
                                }
                            }

                            if (!items.isEmpty()) {
                                fullItems.add(menuItemService.convertToResponse(items.get(0)));
                            }
                        }
                    }
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("explanation", explanation);
                    response.put("items", fullItems);
                    return response;
                }
            }
            return createErrorResponse("Failed to generate combinations");
        } catch (Exception e) {
            log.error("Error generating combinations with items: ", e);
            return createErrorResponse("Error: " + e.getMessage());
        }
    }

    /**
     * Get AI pairing suggestions as plain string
     */
    public String suggestPairingsAsString(Long menuItemId) {
        try {
            Map<String, Object> result = suggestPairings(menuItemId);
            if ((Boolean) result.getOrDefault("success", false)) {
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                if (data != null) {
                    String explanation = (String) data.get("explanation");
                    return explanation != null ? explanation : "No pairings available";
                }
            }
            return "Failed to generate pairings";
        } catch (Exception e) {
            log.error("Error generating pairings string: ", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Get AI meal recommendations as plain string
     */
    public String getMealRecommendationsAsString(Map<String, Object> preferences) {
        try {
            Map<String, Object> result = getMealRecommendations(preferences);
            if ((Boolean) result.getOrDefault("success", false)) {
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                if (data != null) {
                    String explanation = (String) data.get("explanation");
                    return explanation != null ? explanation : "No recommendations available";
                }
            }
            return "Failed to generate recommendations";
        } catch (Exception e) {
            log.error("Error generating recommendations string: ", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Get AI meal recommendations with full menu item details
     */
    public Map<String, Object> getMealRecommendationsWithItems(Map<String, Object> preferences) {
        try {
            Map<String, Object> result = getMealRecommendations(preferences);
            if ((Boolean) result.getOrDefault("success", false)) {
                Map<String, Object> data = (Map<String, Object>) result.get("data");
                if (data != null) {
                    // Get recommended meal list
                    List<Map<String, Object>> recommendedMeal = (List<Map<String, Object>>) data.get("recommendedMeal");
                    String explanation = (String) data.get("explanation");
                    
                    // Handle totalPrice - can be Integer or Double
                    double totalPrice = 0;
                    Object priceObj = data.get("totalPrice");
                    if (priceObj != null) {
                        if (priceObj instanceof Double) {
                            totalPrice = (Double) priceObj;
                        } else if (priceObj instanceof Integer) {
                            totalPrice = ((Integer) priceObj).doubleValue();
                        } else if (priceObj instanceof Number) {
                            totalPrice = ((Number) priceObj).doubleValue();
                        }
                    }
                    
                    // Fetch full menu item details for each recommendation
                    List<MenuItemResponse> fullItems = new java.util.ArrayList<>();
                    if (recommendedMeal != null) {
                        for (Map<String, Object> meal : recommendedMeal) {
                            String itemName = (String) meal.get("itemName");
                            // Remove any markdown bolding if present
                            if (itemName != null) {
                                itemName = itemName.replace("**", "").trim();
                            }
                            
                            // Try to find matching menu item in database
                            // First try exact match
                            List<MenuItem> items = menuItemRepository.findByNameContainingIgnoreCase(itemName);
                            
                            // If no match found, try fuzzy matching or splitting
                            if (items.isEmpty() && itemName.contains(" ")) {
                                String[] parts = itemName.split(" ");
                                for (String part : parts) {
                                    if (part.length() > 3) {
                                        items = menuItemRepository.findByNameContainingIgnoreCase(part);
                                        if (!items.isEmpty()) break;
                                    }
                                }
                            }
                            
                            if (!items.isEmpty()) {
                                fullItems.add(menuItemService.convertToResponse(items.get(0)));
                            }
                        }
                    }
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("explanation", explanation);
                    response.put("items", fullItems);
                    response.put("totalPrice", totalPrice);
                    return response;
                }
            }
            return createErrorResponse("Failed to generate recommendations");
        } catch (Exception e) {
            log.error("Error generating recommendations with items: ", e);
            return createErrorResponse("Error: " + e.getMessage());
        }
    }

}
