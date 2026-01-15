package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIMealItem {
    private Long menuItemId;
    private String name;
    private Double price;
    private String reason;
    private Integer quantity;
    private Long chefId;
    private String chefName;
}
