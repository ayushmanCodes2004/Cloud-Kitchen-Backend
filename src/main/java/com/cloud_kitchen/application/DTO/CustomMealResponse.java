package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomMealResponse {
    private Long id;
    private Long studentId;
    private String name;
    private String description;
    private Double totalPrice;
    private Boolean aiGenerated;
    private String aiPrompt;
    private Double nutritionalScore;
    private Integer timesOrdered;
    private List<CustomMealItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
