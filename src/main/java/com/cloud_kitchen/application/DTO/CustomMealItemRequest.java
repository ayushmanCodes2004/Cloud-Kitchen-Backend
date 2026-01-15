package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomMealItemRequest {
    private Long menuItemId;
    private Integer quantity;
    private String aiReason;
}
