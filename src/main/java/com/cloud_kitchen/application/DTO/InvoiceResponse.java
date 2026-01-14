package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private String invoiceNumber;
    private String orderNumber;
    private LocalDateTime invoiceDate;
    private LocalDateTime orderDate;
    
    // Customer details
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String deliveryAddress;
    
    // Order items
    private List<InvoiceItemDTO> items;
    
    // Amounts
    private Double subtotal;
    private Double taxAmount;
    private Double platformFee;
    private Double totalAmount;
    
    // Payment
    private String paymentMethod;
    private String paymentStatus;
    
    // Additional info
    private String specialInstructions;
    private String status;
}
