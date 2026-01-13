package com.cloud_kitchen.application.Repository;

import com.cloud_kitchen.application.Entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findByChatSessionIdOrderBySentAtAsc(Long chatSessionId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatSessionId = :chatSessionId AND cm.senderUserId = :userId ORDER BY cm.sentAt ASC")
    List<ChatMessage> findByChatSessionIdAndSenderId(@Param("chatSessionId") Long chatSessionId, @Param("userId") Long userId);
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm WHERE cm.chatSessionId = :chatSessionId AND cm.senderUserId != :userId AND cm.readStatus = false")
    Long countUnreadMessages(@Param("chatSessionId") Long chatSessionId, @Param("userId") Long userId);
}