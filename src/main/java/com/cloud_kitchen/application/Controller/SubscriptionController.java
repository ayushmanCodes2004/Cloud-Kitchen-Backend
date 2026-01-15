package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.SubscriptionRequest;
import com.cloud_kitchen.application.DTO.SubscriptionResponse;
import com.cloud_kitchen.application.Entity.SubscriptionPlan;
import com.cloud_kitchen.application.Service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@CrossOrigin(origins = "*")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Subscription API is running");
    }

    /**
     * Get all active subscription plans
     */
    @GetMapping("/plans")
    public ResponseEntity<List<SubscriptionPlan>> getAllPlans() {
        return ResponseEntity.ok(subscriptionService.getAllActivePlans());
    }

    /**
     * Get Gold plan
     */
    @GetMapping("/plans/gold")
    public ResponseEntity<SubscriptionPlan> getGoldPlan() {
        try {
            SubscriptionPlan plan = subscriptionService.getGoldPlan();
            return ResponseEntity.ok(plan);
        } catch (Exception e) {
            System.err.println("Error fetching Gold plan: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch Gold plan: " + e.getMessage());
        }
    }

    /**
     * Create subscription request (Student)
     */
    @PostMapping("/request")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubscriptionResponse> createSubscriptionRequest(@RequestBody SubscriptionRequest request) {
        return ResponseEntity.ok(subscriptionService.createSubscriptionRequest(request));
    }

    /**
     * Get student's subscriptions
     */
    @GetMapping("/my-subscriptions")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<SubscriptionResponse>> getMySubscriptions() {
        return ResponseEntity.ok(subscriptionService.getStudentSubscriptions());
    }

    /**
     * Get student's active subscription
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubscriptionResponse> getActiveSubscription() {
        SubscriptionResponse subscription = subscriptionService.getActiveSubscription();
        if (subscription == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(subscription);
    }

    /**
     * Get all pending subscription requests (Admin)
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubscriptionResponse>> getPendingSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getPendingSubscriptions());
    }

    /**
     * Get all subscriptions (Admin)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions() {
        return ResponseEntity.ok(subscriptionService.getAllSubscriptions());
    }

    /**
     * Approve subscription (Admin)
     */
    @PostMapping("/{subscriptionId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> approveSubscription(
            @PathVariable Long subscriptionId,
            @RequestParam Long adminId) {
        return ResponseEntity.ok(subscriptionService.approveSubscription(subscriptionId, adminId));
    }

    /**
     * Reject subscription (Admin)
     */
    @PostMapping("/{subscriptionId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> rejectSubscription(
            @PathVariable Long subscriptionId,
            @RequestParam String reason,
            @RequestParam Long adminId) {
        return ResponseEntity.ok(subscriptionService.rejectSubscription(subscriptionId, reason, adminId));
    }

    /**
     * Disable/Suspend active subscription (Admin)
     */
    @PostMapping("/{subscriptionId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponse> disableSubscription(
            @PathVariable Long subscriptionId,
            @RequestParam String reason,
            @RequestParam Long adminId) {
        return ResponseEntity.ok(subscriptionService.disableSubscription(subscriptionId, reason, adminId));
    }

    /**
     * Delete subscription (Admin)
     */
    @DeleteMapping("/{subscriptionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSubscription(@PathVariable Long subscriptionId) {
        subscriptionService.deleteSubscription(subscriptionId);
        return ResponseEntity.noContent().build();
    }
}
