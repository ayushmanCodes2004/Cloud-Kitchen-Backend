package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String imageUrl;
    private Boolean available;
    private Boolean vegetarian;
    private Integer preparationTime;
    private String chefName;
    private Long chefId;
}
