package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceItemDTO {
    private String itemName;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private String chefName;
}
