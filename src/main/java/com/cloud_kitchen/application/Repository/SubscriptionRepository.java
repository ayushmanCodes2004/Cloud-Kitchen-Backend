package com.cloud_kitchen.application.Repository;

import com.cloud_kitchen.application.Entity.Subscription;
import com.cloud_kitchen.application.Entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    List<Subscription> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    
    Optional<Subscription> findByStudentIdAndStatus(Long studentId, SubscriptionStatus status);
    
    List<Subscription> findByStatus(SubscriptionStatus status);
    
    List<Subscription> findByStatusOrderByCreatedAtDesc(SubscriptionStatus status);
}
