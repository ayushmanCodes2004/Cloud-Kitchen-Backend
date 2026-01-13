package com.cloud_kitchen.application.Service;

import com.cloud_kitchen.application.DTO.ChatMessageDto;
import com.cloud_kitchen.application.Entity.User;
import com.cloud_kitchen.application.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final AuthService authService;
    private final UserRepository userRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Extract order ID and user ID from the session URI
        String uri = session.getUri().toString();
        String[] parts = uri.split("/");
        
        if (parts.length >= 5) {
            String orderId = parts[parts.length - 2]; // /ws/chat/order/{orderId}/{userId}
            String userId = parts[parts.length - 1];
            
            // Store session with a key combining order and user
            String sessionKey = "order:" + orderId + ":user:" + userId;
            sessions.put(sessionKey, session);
            
            log.info("Chat session established for order {} and user {}", orderId, userId);
            
            // Send welcome message
            ChatMessageDto welcomeMsg = new ChatMessageDto();
            welcomeMsg.setMessage("Chat connection established successfully!");
            welcomeMsg.setMessageType("SYSTEM");
            welcomeMsg.setSenderName("System");
            welcomeMsg.setSentAt(java.time.LocalDateTime.now());
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcomeMsg)));
        } else {
            log.warn("Invalid WebSocket URI: {}", uri);
            session.close();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received message: {}", payload);
        
        try {
            // Parse the incoming message
            IncomingChatMessage incomingMsg = objectMapper.readValue(payload, IncomingChatMessage.class);
            
            // Validate that the user is authorized to send messages for this order
            if (!chatService.isUserAuthorizedForChat(incomingMsg.getOrderId(), incomingMsg.getUserId())) {
                log.warn("Unauthorized chat access attempt for order {} by user {}", 
                        incomingMsg.getOrderId(), incomingMsg.getUserId());
                
                // Send error message back to sender
                ChatMessageDto errorMsg = new ChatMessageDto();
                errorMsg.setMessage("You are not authorized to chat for this order.");
                errorMsg.setMessageType("SYSTEM");
                errorMsg.setSenderName("System");
                errorMsg.setSentAt(java.time.LocalDateTime.now());
                
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMsg)));
                return;
            }
            
            // Check if chat is enabled for this order
            if (!chatService.isChatEnabledForOrder(incomingMsg.getOrderId())) {
                log.warn("Chat is not enabled for order {}", incomingMsg.getOrderId());
                
                // Send error message back to sender
                ChatMessageDto errorMsg = new ChatMessageDto();
                errorMsg.setMessage("Chat is not available for this order at this time.");
                errorMsg.setMessageType("SYSTEM");
                errorMsg.setSenderName("System");
                errorMsg.setSentAt(java.time.LocalDateTime.now());
                
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMsg)));
                return;
            }
            
            // Save the message to the database
            ChatMessageDto savedMessage = chatService.sendMessage(incomingMsg.getOrderId(), incomingMsg.getMessage());
            
            // Send the message to all participants in this order's chat
            broadcastMessageToOrder(incomingMsg.getOrderId(), savedMessage);
            
        } catch (Exception e) {
            log.error("Error processing chat message: ", e);
            
            // Send error message back to sender
            ChatMessageDto errorMsg = new ChatMessageDto();
            errorMsg.setMessage("Error sending message: " + e.getMessage());
            errorMsg.setMessageType("SYSTEM");
            errorMsg.setSenderName("System");
            errorMsg.setSentAt(java.time.LocalDateTime.now());
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMsg)));
        }
    }

    private void broadcastMessageToOrder(Long orderId, ChatMessageDto message) throws IOException {
        // Find all sessions for this order
        sessions.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("order:" + orderId + ":user:"))
                .forEach(entry -> {
                    try {
                        if (entry.getValue().isOpen()) {
                            entry.getValue().sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                        }
                    } catch (IOException e) {
                        log.error("Error broadcasting message to session: ", e);
                    }
                });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("Chat session closed: {}", status);
        
        // Remove session from our map
        sessions.values().remove(session);
    }

    // Inner class for incoming messages
    public static class IncomingChatMessage {
        private Long orderId;
        private Long userId;
        private String message;
        
        // Getters and setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}