package com.cloud_kitchen.application.DTO;

import com.cloud_kitchen.application.Entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    
    private Long id;
    private Long chatSessionId;
    private Long senderUserId;
    private String senderName;
    private String message;
    private String messageType;
    private LocalDateTime sentAt;
    private Boolean readStatus;
    
    public static ChatMessageDto fromEntity(ChatMessage entity, String senderName) {
        ChatMessageDto dto = new ChatMessageDto();
        dto.setId(entity.getId());
        dto.setChatSessionId(entity.getChatSessionId());
        dto.setSenderUserId(entity.getSenderUserId());
        dto.setSenderName(senderName);
        dto.setMessage(entity.getMessage());
        dto.setMessageType(entity.getMessageType().name());
        dto.setSentAt(entity.getSentAt());
        dto.setReadStatus(entity.getReadStatus());
        return dto;
    }
}