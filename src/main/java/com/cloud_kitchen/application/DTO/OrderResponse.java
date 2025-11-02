package com.cloud_kitchen.application.DTO;

import com.cloud_kitchen.application.Entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long studentId;
    private String studentName;
    private Double totalAmount;
    private OrderStatus status;
    private String deliveryAddress;
    private String specialInstructions;
    private LocalDateTime estimatedDeliveryTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponse> orderItems;
}
