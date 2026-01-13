package com.cloud_kitchen.application.Repository;

import com.cloud_kitchen.application.Entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    
    Optional<ChatSession> findByOrderId(Long orderId);
    
    List<ChatSession> findByOrderIdIn(List<Long> orderIds);
    
    List<ChatSession> findByCreatedByStudentIdOrAssignedChefId(Long studentId, Long chefId);
    
    @Query(value = "SELECT * FROM chat_sessions WHERE order_id = :orderId AND status = 'ACTIVE'", nativeQuery = true)
    Optional<ChatSession> findActiveChatSessionByOrderId(@Param("orderId") Long orderId);
}