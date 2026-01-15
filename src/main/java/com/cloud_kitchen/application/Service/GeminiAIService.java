package com.cloud_kitchen.application.Service;

import com.cloud_kitchen.application.DTO.AIMealGenerationRequest;
import com.cloud_kitchen.application.DTO.AIMealGenerationResponse;
import com.cloud_kitchen.application.DTO.AIMealItem;
import com.cloud_kitchen.application.Entity.MenuItem;
import com.cloud_kitchen.application.Repository.MenuItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeminiAIService {

    private static final Logger log = LoggerFactory.getLogger(GeminiAIService.class);

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Autowired
    private MenuItemRepository menuItemRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generate AI meal based on user input
     */
    public AIMealGenerationResponse generateAIMeal(AIMealGenerationRequest request) {
        try {
            // Get available menu items
            List<MenuItem> availableItems = menuItemRepository.findByAvailable(true);

            // Create prompt for Gemini
            String prompt = createMealGenerationPrompt(request, availableItems);

            // Call Gemini API
            String geminiResponse = callGeminiApi(prompt);

            // Parse response
            return parseMealGenerationResponse(geminiResponse, availableItems);

        } catch (Exception e) {
            log.error("AI meal generation failed: {}", e.getMessage());
            // Return fallback meal
            return generateFallbackMeal(request);
        }
    }

    /**
     * Get smart recommendations for current items
     */
    public List<AIMealItem> getSmartRecommendations(List<Long> currentItemIds) {
        try {
            List<MenuItem> currentItems = menuItemRepository.findAllById(currentItemIds);
            List<MenuItem> allItems = menuItemRepository.findByAvailable(true);

            // Remove current items from recommendations
            List<MenuItem> availableForRecommendation = allItems.stream()
                    .filter(item -> !currentItemIds.contains(item.getId()))
                    .collect(Collectors.toList());

            String prompt = createRecommendationPrompt(currentItems, availableForRecommendation);
            String geminiResponse = callGeminiApi(prompt);

            return parseRecommendationResponse(geminiResponse, availableForRecommendation);

        } catch (Exception e) {
            log.error("Smart recommendations failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Analyze meal and provide insights
     */
    public Map<String, Object> analyzeMeal(String mealName, List<Long> itemIds) {
        try {
            List<MenuItem> items = menuItemRepository.findAllById(itemIds);

            String prompt = createAnalysisPrompt(mealName, items);
            String geminiResponse = callGeminiApi(prompt);

            return parseAnalysisResponse(geminiResponse);

        } catch (Exception e) {
            log.error("Meal analysis failed: {}", e.getMessage());
            return createFallbackAnalysis();
        }
    }

    /**
     * Create meal generation prompt
     */
    private String createMealGenerationPrompt(AIMealGenerationRequest request, List<MenuItem> availableItems) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a professional nutritionist and chef assistant for a cloud kitchen platform.\n\n");
        
        // Filter items based on dietary preferences BEFORE showing to AI
        List<MenuItem> filteredItems = filterItemsByDietaryPreferences(availableItems, request.getDietaryPreferences());
        
        prompt.append("User Request: \"").append(request.getUserInput()).append("\"\n\n");

        if (request.getBudget() != null) {
            prompt.append("Budget Constraint: ₹").append(request.getBudget()).append(" (STRICT - DO NOT EXCEED)\n");
        }

        // STRICT dietary preferences section
        if (request.getDietaryPreferences() != null && !request.getDietaryPreferences().isEmpty()) {
            prompt.append("\n⚠️ CRITICAL DIETARY RESTRICTIONS (MUST FOLLOW STRICTLY):\n");
            for (String pref : request.getDietaryPreferences()) {
                String prefLower = pref.toLowerCase();
                if (prefLower.contains("veg") && !prefLower.contains("non")) {
                    prompt.append("- VEGETARIAN ONLY: Absolutely NO meat, chicken, fish, seafood, eggs, or any animal flesh\n");
                    prompt.append("- Only select items that are 100% vegetarian\n");
                } else if (prefLower.contains("vegan")) {
                    prompt.append("- VEGAN ONLY: NO meat, dairy, eggs, honey, or any animal products whatsoever\n");
                    prompt.append("- Only plant-based items allowed\n");
                } else if (prefLower.contains("jain")) {
                    prompt.append("- JAIN DIET: NO onion, garlic, root vegetables (potato, carrot, radish), meat, eggs\n");
                    prompt.append("- Only Jain-compliant items allowed\n");
                } else if (prefLower.contains("non-veg") || prefLower.contains("nonveg")) {
                    prompt.append("- NON-VEGETARIAN: Can include meat, chicken, fish, seafood\n");
                } else {
                    prompt.append("- ").append(pref).append("\n");
                }
            }
            prompt.append("⚠️ VIOLATION OF THESE RESTRICTIONS IS UNACCEPTABLE\n\n");
        }

        if (request.getAllergies() != null && !request.getAllergies().isEmpty()) {
            prompt.append("ALLERGIES (MUST AVOID): ").append(String.join(", ", request.getAllergies())).append("\n\n");
        }

        if (request.getOccasion() != null) {
            prompt.append("Occasion: ").append(request.getOccasion()).append("\n\n");
        }

        prompt.append("Available Menu Items (PRE-FILTERED for dietary restrictions):\n");
        for (MenuItem item : filteredItems) {
            prompt.append(String.format("- %s (₹%.2f) - %s, Chef: %s\n",
                    item.getName(), item.getPrice(), item.getCategory(), item.getChef().getName()));
        }

        prompt.append("\n🎯 STRICT REQUIREMENTS:\n");
        prompt.append("1. ONLY select items from the list above - NO exceptions\n");
        prompt.append("2. STRICTLY follow dietary restrictions - this is non-negotiable\n");
        prompt.append("3. Stay within budget (if specified) - do not exceed even by ₹1\n");
        prompt.append("4. Respect all allergies - exclude any items with allergens\n");
        prompt.append("5. Create a nutritionally balanced meal\n");
        prompt.append("6. Select 3-5 items maximum for optimal combination\n");
        prompt.append("7. Ensure complementary flavors and textures\n\n");

        prompt.append("Return ONLY a valid JSON response (no markdown, no code blocks) with this exact structure:\n");
        prompt.append("{\n");
        prompt.append("  \"mealName\": \"Creative meal name\",\n");
        prompt.append("  \"description\": \"Why this meal is perfect for the request\",\n");
        prompt.append("  \"items\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"name\": \"Exact item name from menu\",\n");
        prompt.append("      \"reason\": \"Why this item was chosen\",\n");
        prompt.append("      \"quantity\": 1\n");
        prompt.append("    }\n");
        prompt.append("  ],\n");
        prompt.append("  \"nutritionalInfo\": {\n");
        prompt.append("    \"calories\": 650,\n");
        prompt.append("    \"protein\": 35,\n");
        prompt.append("    \"carbs\": 75,\n");
        prompt.append("    \"fat\": 20\n");
        prompt.append("  },\n");
        prompt.append("  \"tags\": [\"high-protein\", \"post-workout\"],\n");
        prompt.append("  \"nutritionalScore\": 8.5\n");
        prompt.append("}\n");

        return prompt.toString();
    }
    
    /**
     * Filter menu items based on dietary preferences
     */
    private List<MenuItem> filterItemsByDietaryPreferences(List<MenuItem> items, List<String> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            return items;
        }
        
        List<MenuItem> filtered = new ArrayList<>(items);
        
        for (String pref : preferences) {
            String prefLower = pref.toLowerCase();
            
            if (prefLower.contains("veg") && !prefLower.contains("non")) {
                // Vegetarian: exclude non-veg items
                filtered = filtered.stream()
                    .filter(item -> {
                        String name = item.getName().toLowerCase();
                        String category = item.getCategory().toLowerCase();
                        // Exclude if contains meat/chicken/fish/egg keywords
                        return !name.contains("chicken") && !name.contains("mutton") 
                            && !name.contains("fish") && !name.contains("prawn") 
                            && !name.contains("egg") && !name.contains("meat")
                            && !category.contains("non-veg") && !category.contains("nonveg");
                    })
                    .collect(Collectors.toList());
            }
            
            if (prefLower.contains("vegan")) {
                // Vegan: exclude dairy, eggs, and non-veg
                filtered = filtered.stream()
                    .filter(item -> {
                        String name = item.getName().toLowerCase();
                        return !name.contains("chicken") && !name.contains("mutton") 
                            && !name.contains("fish") && !name.contains("egg")
                            && !name.contains("paneer") && !name.contains("cheese")
                            && !name.contains("butter") && !name.contains("ghee")
                            && !name.contains("milk") && !name.contains("curd")
                            && !name.contains("yogurt") && !name.contains("cream");
                    })
                    .collect(Collectors.toList());
            }
            
            if (prefLower.contains("jain")) {
                // Jain: exclude onion, garlic, root vegetables, non-veg
                filtered = filtered.stream()
                    .filter(item -> {
                        String name = item.getName().toLowerCase();
                        return !name.contains("onion") && !name.contains("garlic") 
                            && !name.contains("potato") && !name.contains("carrot")
                            && !name.contains("radish") && !name.contains("beetroot")
                            && !name.contains("chicken") && !name.contains("mutton")
                            && !name.contains("fish") && !name.contains("egg");
                    })
                    .collect(Collectors.toList());
            }
        }
        
        return filtered;
    }

    /**
     * Create recommendation prompt
     */
    private String createRecommendationPrompt(List<MenuItem> currentItems, List<MenuItem> availableItems) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Current meal items:\n");
        for (MenuItem item : currentItems) {
            prompt.append(String.format("- %s (₹%.2f)\n", item.getName(), item.getPrice()));
        }

        prompt.append("\nAvailable items to recommend:\n");
        for (MenuItem item : availableItems) {
            prompt.append(String.format("- %s (₹%.2f) - %s\n", item.getName(), item.getPrice(), item.getCategory()));
        }

        prompt.append("\nSuggest 3 items that would pair perfectly with the current selection.\n");
        prompt.append("Consider flavor profiles, nutritional balance, and traditional pairings.\n\n");

        prompt.append("Return ONLY a valid JSON array (no markdown, no code blocks):\n");
        prompt.append("[\n");
        prompt.append("  {\n");
        prompt.append("    \"name\": \"Exact item name from menu\",\n");
        prompt.append("    \"matchScore\": 95,\n");
        prompt.append("    \"reason\": \"Why it pairs well\"\n");
        prompt.append("  }\n");
        prompt.append("]\n");

        return prompt.toString();
    }

    /**
     * Create analysis prompt
     */
    private String createAnalysisPrompt(String mealName, List<MenuItem> items) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this meal:\n");
        prompt.append("Name: ").append(mealName).append("\n");
        prompt.append("Items:\n");
        for (MenuItem item : items) {
            prompt.append(String.format("- %s (₹%.2f) - %s\n", item.getName(), item.getPrice(), item.getCategory()));
        }

        prompt.append("\nProvide:\n");
        prompt.append("1. Nutritional score (0-10)\n");
        prompt.append("2. Strengths (what's good)\n");
        prompt.append("3. Suggestions (what could be better)\n");
        prompt.append("4. Best occasions for this meal\n");
        prompt.append("5. Health benefits\n\n");

        prompt.append("Return ONLY a valid JSON response (no markdown, no code blocks):\n");
        prompt.append("{\n");
        prompt.append("  \"score\": 8.5,\n");
        prompt.append("  \"strengths\": [\"High protein\", \"Balanced\"],\n");
        prompt.append("  \"suggestions\": [\"Add fruit\", \"More fiber\"],\n");
        prompt.append("  \"bestFor\": [\"Post-workout\", \"Breakfast\"],\n");
        prompt.append("  \"healthBenefits\": [\"Muscle recovery\", \"Energy\"]\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    /**
     * Call Gemini API
     */
    private String callGeminiApi(String prompt) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> contents = new ArrayList<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, String>> parts = new ArrayList<>();
            Map<String, String> part = new HashMap<>();
            part.put("text", prompt);
            parts.add(part);
            content.put("parts", parts);
            contents.add(content);
            requestBody.put("contents", contents);

            String url = geminiApiUrl + "?key=" + geminiApiKey;
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode content1 = candidates.get(0).path("content");
                    JsonNode parts1 = content1.path("parts");
                    if (parts1.isArray() && parts1.size() > 0) {
                        return parts1.get(0).path("text").asText();
                    }
                }
            }

            throw new RuntimeException("Failed to get response from Gemini API");

        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage());
            throw new RuntimeException("Gemini API call failed", e);
        }
    }

    /**
     * Parse meal generation response
     */
    private AIMealGenerationResponse parseMealGenerationResponse(String geminiResponse, List<MenuItem> availableItems) {
        try {
            // Clean response - remove markdown code blocks if present
            String cleanedResponse = geminiResponse.trim();
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.substring(7);
            }
            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.substring(3);
            }
            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
            }
            cleanedResponse = cleanedResponse.trim();

            JsonNode root = objectMapper.readTree(cleanedResponse);

            AIMealGenerationResponse response = new AIMealGenerationResponse();
            response.setMealName(root.path("mealName").asText());
            response.setDescription(root.path("description").asText());

            // Parse items
            List<AIMealItem> items = new ArrayList<>();
            JsonNode itemsNode = root.path("items");
            double totalPrice = 0.0;

            for (JsonNode itemNode : itemsNode) {
                String itemName = itemNode.path("name").asText();
                String reason = itemNode.path("reason").asText();
                int quantity = itemNode.path("quantity").asInt(1);

                // Find matching menu item
                MenuItem menuItem = availableItems.stream()
                        .filter(mi -> mi.getName().equalsIgnoreCase(itemName))
                        .findFirst()
                        .orElse(null);

                if (menuItem != null) {
                    AIMealItem aiItem = new AIMealItem();
                    aiItem.setMenuItemId(menuItem.getId());
                    aiItem.setName(menuItem.getName());
                    aiItem.setPrice(menuItem.getPrice());
                    aiItem.setReason(reason);
                    aiItem.setQuantity(quantity);
                    aiItem.setChefId(menuItem.getChef().getId());
                    aiItem.setChefName(menuItem.getChef().getName());
                    items.add(aiItem);

                    totalPrice += menuItem.getPrice() * quantity;
                }
            }

            response.setItems(items);
            response.setTotalPrice(totalPrice);

            // Parse nutritional info
            JsonNode nutritionalNode = root.path("nutritionalInfo");
            Map<String, Object> nutritionalInfo = new HashMap<>();
            nutritionalInfo.put("calories", nutritionalNode.path("calories").asInt());
            nutritionalInfo.put("protein", nutritionalNode.path("protein").asInt());
            nutritionalInfo.put("carbs", nutritionalNode.path("carbs").asInt());
            nutritionalInfo.put("fat", nutritionalNode.path("fat").asInt());
            response.setNutritionalInfo(nutritionalInfo);

            // Parse tags
            List<String> tags = new ArrayList<>();
            JsonNode tagsNode = root.path("tags");
            for (JsonNode tagNode : tagsNode) {
                tags.add(tagNode.asText());
            }
            response.setTags(tags);

            response.setNutritionalScore(root.path("nutritionalScore").asDouble(8.0));

            return response;

        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse AI response", e);
        }
    }

    /**
     * Parse recommendation response
     */
    private List<AIMealItem> parseRecommendationResponse(String geminiResponse, List<MenuItem> availableItems) {
        try {
            String cleanedResponse = geminiResponse.trim();
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.substring(7);
            }
            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.substring(3);
            }
            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
            }
            cleanedResponse = cleanedResponse.trim();

            JsonNode root = objectMapper.readTree(cleanedResponse);

            List<AIMealItem> recommendations = new ArrayList<>();
            for (JsonNode itemNode : root) {
                String itemName = itemNode.path("name").asText();
                String reason = itemNode.path("reason").asText();

                MenuItem menuItem = availableItems.stream()
                        .filter(mi -> mi.getName().equalsIgnoreCase(itemName))
                        .findFirst()
                        .orElse(null);

                if (menuItem != null) {
                    AIMealItem aiItem = new AIMealItem();
                    aiItem.setMenuItemId(menuItem.getId());
                    aiItem.setName(menuItem.getName());
                    aiItem.setPrice(menuItem.getPrice());
                    aiItem.setReason(reason);
                    aiItem.setQuantity(1);
                    aiItem.setChefId(menuItem.getChef().getId());
                    aiItem.setChefName(menuItem.getChef().getName());
                    recommendations.add(aiItem);
                }
            }

            return recommendations;

        } catch (Exception e) {
            log.error("Failed to parse recommendations: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Parse analysis response
     */
    private Map<String, Object> parseAnalysisResponse(String geminiResponse) {
        try {
            String cleanedResponse = geminiResponse.trim();
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.substring(7);
            }
            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.substring(3);
            }
            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
            }
            cleanedResponse = cleanedResponse.trim();

            JsonNode root = objectMapper.readTree(cleanedResponse);

            Map<String, Object> analysis = new HashMap<>();
            analysis.put("score", root.path("score").asDouble());

            List<String> strengths = new ArrayList<>();
            for (JsonNode node : root.path("strengths")) {
                strengths.add(node.asText());
            }
            analysis.put("strengths", strengths);

            List<String> suggestions = new ArrayList<>();
            for (JsonNode node : root.path("suggestions")) {
                suggestions.add(node.asText());
            }
            analysis.put("suggestions", suggestions);

            List<String> bestFor = new ArrayList<>();
            for (JsonNode node : root.path("bestFor")) {
                bestFor.add(node.asText());
            }
            analysis.put("bestFor", bestFor);

            List<String> healthBenefits = new ArrayList<>();
            for (JsonNode node : root.path("healthBenefits")) {
                healthBenefits.add(node.asText());
            }
            analysis.put("healthBenefits", healthBenefits);

            return analysis;

        } catch (Exception e) {
            log.error("Failed to parse analysis: {}", e.getMessage());
            return createFallbackAnalysis();
        }
    }

    /**
     * Generate fallback meal when AI fails
     */
    private AIMealGenerationResponse generateFallbackMeal(AIMealGenerationRequest request) {
        List<MenuItem> availableItems = menuItemRepository.findByAvailable(true);

        // Select random items within budget
        List<MenuItem> selectedItems = new ArrayList<>();
        double totalPrice = 0.0;
        double budget = request.getBudget() != null ? request.getBudget() : 300.0;

        Collections.shuffle(availableItems);
        for (MenuItem item : availableItems) {
            if (totalPrice + item.getPrice() <= budget && selectedItems.size() < 3) {
                selectedItems.add(item);
                totalPrice += item.getPrice();
            }
        }

        AIMealGenerationResponse response = new AIMealGenerationResponse();
        response.setMealName("Quick Meal Combo");
        response.setDescription("A delicious combination of our popular items");

        List<AIMealItem> items = selectedItems.stream().map(item -> {
            AIMealItem aiItem = new AIMealItem();
            aiItem.setMenuItemId(item.getId());
            aiItem.setName(item.getName());
            aiItem.setPrice(item.getPrice());
            aiItem.setReason("Popular choice");
            aiItem.setQuantity(1);
            aiItem.setChefId(item.getChef().getId());
            aiItem.setChefName(item.getChef().getName());
            return aiItem;
        }).collect(Collectors.toList());

        response.setItems(items);
        response.setTotalPrice(totalPrice);
        response.setTags(Arrays.asList("quick", "popular"));
        response.setNutritionalScore(7.5);

        Map<String, Object> nutritionalInfo = new HashMap<>();
        nutritionalInfo.put("calories", 600);
        nutritionalInfo.put("protein", 30);
        nutritionalInfo.put("carbs", 70);
        nutritionalInfo.put("fat", 20);
        response.setNutritionalInfo(nutritionalInfo);

        return response;
    }

    /**
     * Create fallback analysis
     */
    private Map<String, Object> createFallbackAnalysis() {
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("score", 7.5);
        analysis.put("strengths", Arrays.asList("Good variety", "Balanced portions"));
        analysis.put("suggestions", Arrays.asList("Consider adding vegetables", "Stay hydrated"));
        analysis.put("bestFor", Arrays.asList("Regular meals", "Everyday dining"));
        analysis.put("healthBenefits", Arrays.asList("Provides energy", "Satisfying meal"));
        return analysis;
    }
}
