package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomMealRequest {
    private String name;
    private String description;
    private Boolean aiGenerated;
    private String aiPrompt;
    private List<CustomMealItemRequest> items;
}
