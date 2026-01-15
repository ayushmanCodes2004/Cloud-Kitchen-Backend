package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIMealGenerationResponse {
    private String mealName;
    private String description;
    private List<AIMealItem> items;
    private Map<String, Object> nutritionalInfo;
    private Double totalPrice;
    private List<String> tags;
    private Double nutritionalScore;
}
