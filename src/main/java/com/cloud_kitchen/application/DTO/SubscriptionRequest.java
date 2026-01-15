package com.cloud_kitchen.application.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRequest {
    private Long planId;
    private String paymentInvoiceUrl;
    private String paymentMethod;
    private String transactionReference;
}
