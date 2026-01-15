package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomMealItemResponse {
    private Long id;
    private Long menuItemId;
    private String menuItemName;
    private Double menuItemPrice;
    private String menuItemImage;
    private Integer quantity;
    private Long chefId;
    private String chefName;
    private String aiReason;
}
