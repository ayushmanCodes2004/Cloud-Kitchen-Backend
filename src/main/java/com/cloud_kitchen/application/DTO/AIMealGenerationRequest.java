package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIMealGenerationRequest {
    private String userInput;
    private Double budget;
    private List<String> dietaryPreferences;
    private List<String> allergies;
    private String occasion;
}
