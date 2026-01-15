package com.cloud_kitchen.application.DTO;

import com.cloud_kitchen.application.Entity.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private Long id;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private Long planId;
    private String planName;
    private Double planPrice;
    private Integer discountPercentage;
    private Boolean platformFeeWaived;
    private SubscriptionStatus status;
    private String paymentInvoiceUrl;
    private String paymentMethod;
    private String transactionReference;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
