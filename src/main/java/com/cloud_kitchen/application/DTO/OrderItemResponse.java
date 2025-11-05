package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private Long id;
    private Long menuItemId;
    private String menuItemName;
    private Integer quantity;
    private Double price;
    private Double subtotal;
    private Boolean vegetarian; // Added for veg/non-veg icon display
    private Long chefId; // Added to identify the chef for each item
    private String chefName;
}
