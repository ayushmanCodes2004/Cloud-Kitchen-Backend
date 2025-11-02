package com.cloud_kitchen.application.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequest> items;

    @NotNull(message = "Delivery address is required")
    private String deliveryAddress;

    private String specialInstructions;
}