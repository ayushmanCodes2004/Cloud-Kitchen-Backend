package com.cloud_kitchen.application.Service;

import com.cloud_kitchen.application.DTO.SubscriptionRequest;
import com.cloud_kitchen.application.DTO.SubscriptionResponse;
import com.cloud_kitchen.application.Entity.*;
import com.cloud_kitchen.application.Repository.StudentRepository;
import com.cloud_kitchen.application.Repository.SubscriptionPlanRepository;
import com.cloud_kitchen.application.Repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Autowired
    private StudentRepository studentRepository;

    /**
     * Get all active subscription plans
     */
    public List<SubscriptionPlan> getAllActivePlans() {
        return subscriptionPlanRepository.findByIsActiveTrue();
    }

    /**
     * Get Gold plan
     */
    public SubscriptionPlan getGoldPlan() {
        System.out.println("Fetching Gold plan from database...");
        try {
            Optional<SubscriptionPlan> plan = subscriptionPlanRepository.findByNameAndIsActiveTrue("Gold");
            if (plan.isPresent()) {
                System.out.println("Gold plan found: " + plan.get().getName());
                return plan.get();
            } else {
                System.err.println("Gold plan not found in database");
                // Try to find any plan with name containing "gold" (case insensitive)
                List<SubscriptionPlan> allPlans = subscriptionPlanRepository.findAll();
                System.out.println("All plans in database: " + allPlans.size());
                for (SubscriptionPlan p : allPlans) {
                    System.out.println("Plan: " + p.getName() + ", Active: " + p.isActive());
                }
                throw new RuntimeException("Gold plan not found. Available plans: " + allPlans.size());
            }
        } catch (Exception e) {
            System.err.println("Error in getGoldPlan: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    /**
     * Create subscription request
     */
    @Transactional
    public SubscriptionResponse createSubscriptionRequest(SubscriptionRequest request) {
        // Get authenticated student
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Check if student already has an active or pending subscription
        List<Subscription> existingSubscriptions = subscriptionRepository.findByStudentIdOrderByCreatedAtDesc(student.getId());
        for (Subscription sub : existingSubscriptions) {
            if (sub.getStatus() == SubscriptionStatus.ACTIVE || sub.getStatus() == SubscriptionStatus.PENDING) {
                throw new RuntimeException("You already have an active or pending subscription");
            }
        }

        // Get subscription plan
        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        // Create subscription
        Subscription subscription = new Subscription();
        subscription.setStudent(student);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.PENDING);
        subscription.setPaymentInvoiceUrl(request.getPaymentInvoiceUrl());
        subscription.setPaymentMethod(request.getPaymentMethod());
        subscription.setTransactionReference(request.getTransactionReference());

        subscription = subscriptionRepository.save(subscription);

        return convertToResponse(subscription);
    }

    /**
     * Get student's subscriptions
     */
    public List<SubscriptionResponse> getStudentSubscriptions() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<Subscription> subscriptions = subscriptionRepository.findByStudentIdOrderByCreatedAtDesc(student.getId());
        return subscriptions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get student's active subscription
     */
    public SubscriptionResponse getActiveSubscription() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Subscription subscription = subscriptionRepository.findByStudentIdAndStatus(student.getId(), SubscriptionStatus.ACTIVE)
                .orElse(null);

        return subscription != null ? convertToResponse(subscription) : null;
    }

    /**
     * Get all pending subscription requests (Admin)
     */
    public List<SubscriptionResponse> getPendingSubscriptions() {
        List<Subscription> subscriptions = subscriptionRepository.findByStatusOrderByCreatedAtDesc(SubscriptionStatus.PENDING);
        return subscriptions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all subscriptions (Admin)
     */
    public List<SubscriptionResponse> getAllSubscriptions() {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        return subscriptions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Approve subscription (Admin)
     */
    @Transactional
    public SubscriptionResponse approveSubscription(Long subscriptionId, Long adminId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            throw new RuntimeException("Only pending subscriptions can be approved");
        }

        // Set subscription dates using IST timezone
        LocalDateTime now = LocalDateTime.now(IST_ZONE);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(now);
        subscription.setEndDate(now.plusDays(subscription.getPlan().getDurationDays()));
        subscription.setApprovedBy(adminId);
        subscription.setApprovedAt(now);

        subscription = subscriptionRepository.save(subscription);

        // Update student subscription status
        Student student = subscription.getStudent();
        student.setSubscriptionStatus("ACTIVE");
        student.setSubscriptionId(subscription.getId());
        studentRepository.save(student);

        return convertToResponse(subscription);
    }

    /**
     * Reject subscription (Admin)
     */
    @Transactional
    public SubscriptionResponse rejectSubscription(Long subscriptionId, String reason, Long adminId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            throw new RuntimeException("Only pending subscriptions can be rejected");
        }

        subscription.setStatus(SubscriptionStatus.REJECTED);
        subscription.setRejectionReason(reason);
        subscription.setApprovedBy(adminId);
        subscription.setApprovedAt(LocalDateTime.now(IST_ZONE));

        subscription = subscriptionRepository.save(subscription);

        return convertToResponse(subscription);
    }

    /**
     * Disable/Suspend active subscription (Admin)
     * This removes Gold Plan benefits immediately
     */
    @Transactional
    public SubscriptionResponse disableSubscription(Long subscriptionId, String reason, Long adminId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new RuntimeException("Only active subscriptions can be disabled");
        }

        // Update subscription status
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setRejectionReason(reason);
        subscription.setApprovedBy(adminId);
        subscription.setUpdatedAt(LocalDateTime.now(IST_ZONE));

        subscription = subscriptionRepository.save(subscription);

        // Update student subscription status - remove benefits
        Student student = subscription.getStudent();
        student.setSubscriptionStatus("NONE");
        student.setSubscriptionId(null);
        studentRepository.save(student);

        return convertToResponse(subscription);
    }

    /**
     * Delete subscription (Admin)
     * Permanently removes subscription record
     */
    @Transactional
    public void deleteSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // If subscription is active, update student status first
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            Student student = subscription.getStudent();
            student.setSubscriptionStatus("NONE");
            student.setSubscriptionId(null);
            studentRepository.save(student);
        }

        // Delete subscription
        subscriptionRepository.delete(subscription);
    }

    /**
     * Check if student has active subscription
     */
    public boolean hasActiveSubscription(Long studentId) {
        return subscriptionRepository.findByStudentIdAndStatus(studentId, SubscriptionStatus.ACTIVE).isPresent();
    }

    /**
     * Get active subscription for student
     */
    public Subscription getActiveSubscriptionForStudent(Long studentId) {
        return subscriptionRepository.findByStudentIdAndStatus(studentId, SubscriptionStatus.ACTIVE)
                .orElse(null);
    }

    /**
     * Convert Subscription to SubscriptionResponse
     */
    private SubscriptionResponse convertToResponse(Subscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(subscription.getId());
        response.setStudentId(subscription.getStudent().getId());
        response.setStudentName(subscription.getStudent().getName());
        response.setStudentEmail(subscription.getStudent().getEmail());
        response.setPlanId(subscription.getPlan().getId());
        response.setPlanName(subscription.getPlan().getName());
        response.setPlanPrice(subscription.getPlan().getPrice());
        response.setDiscountPercentage(subscription.getPlan().getDiscountPercentage());
        response.setPlatformFeeWaived(subscription.getPlan().getPlatformFeeWaived());
        response.setStatus(subscription.getStatus());
        response.setPaymentInvoiceUrl(subscription.getPaymentInvoiceUrl());
        response.setPaymentMethod(subscription.getPaymentMethod());
        response.setTransactionReference(subscription.getTransactionReference());
        response.setStartDate(subscription.getStartDate());
        response.setEndDate(subscription.getEndDate());
        response.setApprovedBy(subscription.getApprovedBy());
        response.setApprovedAt(subscription.getApprovedAt());
        response.setRejectionReason(subscription.getRejectionReason());
        response.setCreatedAt(subscription.getCreatedAt());
        response.setUpdatedAt(subscription.getUpdatedAt());
        return response;
    }
}
