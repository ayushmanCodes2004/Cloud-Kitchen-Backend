package com.cloud_kitchen.application.Entity;

public enum SubscriptionStatus {
    PENDING,    // Waiting for admin approval
    ACTIVE,     // Currently active subscription
    EXPIRED,    // Subscription period ended
    CANCELLED,  // Cancelled by admin or student
    REJECTED    // Payment not verified/rejected by admin
}
