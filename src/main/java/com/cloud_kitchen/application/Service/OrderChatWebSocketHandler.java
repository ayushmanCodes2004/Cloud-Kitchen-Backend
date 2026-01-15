package com.cloud_kitchen.application.Service;

import com.cloud_kitchen.application.DTO.ChatMessageDto;
import com.cloud_kitchen.application.Entity.User;
import com.cloud_kitchen.application.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderChatWebSocketHandler extends TextWebSocketHandler {

    private static final ZoneId IST_ZONE = ZoneId.of("Asia/Kolkata");
    
    private final ChatService chatService;
    private final AuthService authService;
    private final UserRepository userRepository;
    
    // Configure ObjectMapper to handle Java 8 date/time types
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionKey = null;
        try {
            // Extract order ID and user ID from query parameters
            String query = session.getUri().getQuery();
            log.info("=== WebSocket Handler - Connection Established ===");
            log.info("Session ID: {}", session.getId());
            log.info("Query: {}", query);
            log.info("Session isOpen: {}", session.isOpen());
            
            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                String orderId = null;
                String userId = null;
                
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        if ("orderId".equals(keyValue[0])) {
                            orderId = keyValue[1];
                        } else if ("userId".equals(keyValue[0])) {
                            userId = keyValue[1];
                        }
                    }
                }
                
                log.info("Extracted orderId: {}, userId: {}", orderId, userId);
                
                if (orderId != null && userId != null) {
                    // Store session with a key combining order and user
                    sessionKey = "order:" + orderId + ":user:" + userId;
                    sessions.put(sessionKey, session);
                    
                    log.info("✅ Chat session stored with key: {}", sessionKey);
                    log.info("✅ Total active sessions: {}", sessions.size());
                    log.info("✅ Connection fully established - ready to receive messages");
                    
                    // DON'T send welcome message - it might be causing serialization issues
                    // The frontend will know connection is successful from the onopen event
                    
                } else {
                    log.warn("❌ Missing orderId or userId in query parameters: {}", query);
                    session.close(CloseStatus.BAD_DATA.withReason("Missing orderId or userId"));
                }
            } else {
                log.warn("❌ No query parameters provided in WebSocket URI");
                session.close(CloseStatus.BAD_DATA.withReason("No query parameters"));
            }
        } catch (Exception e) {
            log.error("❌ Error in afterConnectionEstablished", e);
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Exception message: {}", e.getMessage());
            log.error("Stack trace: ", e);
            
            // Remove from sessions if we added it
            if (sessionKey != null) {
                sessions.remove(sessionKey);
                log.info("Removed failed session from map");
            }
            
            try {
                if (session.isOpen()) {
                    session.close(CloseStatus.SERVER_ERROR.withReason("Server error: " + e.getMessage()));
                }
            } catch (Exception closeEx) {
                log.error("Error closing session after exception", closeEx);
            }
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
                errorMsg.setSentAt(LocalDateTime.now(IST_ZONE));
                
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
                errorMsg.setSentAt(LocalDateTime.now(IST_ZONE));
                
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMsg)));
                return;
            }
            
            // Save the message to the database
            log.info("Saving message for order {} from user {}", incomingMsg.getOrderId(), incomingMsg.getUserId());
            ChatMessageDto savedMessage = chatService.sendMessage(
                incomingMsg.getOrderId(), 
                incomingMsg.getUserId(), 
                incomingMsg.getMessage()
            );
            log.info("Message saved successfully: {}", savedMessage);
            
            // Send the message to all participants in this order's chat
            log.info("Broadcasting message to all participants in order {}", incomingMsg.getOrderId());
            broadcastMessageToOrder(incomingMsg.getOrderId(), savedMessage);
            
        } catch (Exception e) {
            log.error("Error processing chat message: ", e);
            
            // Send error message back to sender
            ChatMessageDto errorMsg = new ChatMessageDto();
            errorMsg.setMessage("Error sending message: " + e.getMessage());
            errorMsg.setMessageType("SYSTEM");
            errorMsg.setSenderName("System");
            errorMsg.setSentAt(LocalDateTime.now(IST_ZONE));
            
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorMsg)));
        }
    }

    private void broadcastMessageToOrder(Long orderId, ChatMessageDto message) throws IOException {
        log.info("=== Broadcasting Message ===");
        log.info("Order ID: {}", orderId);
        log.info("Message: {}", message);
        log.info("Total active sessions: {}", sessions.size());
        
        // Find all sessions for this order
        List<Map.Entry<String, WebSocketSession>> orderSessions = sessions.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("order:" + orderId + ":user:"))
                .collect(Collectors.toList());
        
        log.info("Found {} sessions for order {}", orderSessions.size(), orderId);
        
        orderSessions.forEach(entry -> {
            log.info("Broadcasting to session: {}", entry.getKey());
            try {
                if (entry.getValue().isOpen()) {
                    String messageJson = objectMapper.writeValueAsString(message);
                    log.info("Sending message JSON: {}", messageJson);
                    entry.getValue().sendMessage(new TextMessage(messageJson));
                    log.info("✅ Message sent successfully to session: {}", entry.getKey());
                } else {
                    log.warn("❌ Session is closed: {}", entry.getKey());
                }
            } catch (IOException e) {
                log.error("❌ Error broadcasting message to session {}: ", entry.getKey(), e);
            }
        });
        
        log.info("=== Broadcast Complete ===");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("=== WebSocket Handler - Connection Closed ===");
        log.info("Session ID: {}", session.getId());
        log.info("Close status code: {}", status.getCode());
        log.info("Close reason: {}", status.getReason());
        log.info("Was clean: {}", status.equals(CloseStatus.NORMAL));
        
        // Remove session from our map
        sessions.values().remove(session);
        log.info("Session removed. Remaining active sessions: {}", sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("=== WebSocket Transport Error ===");
        log.error("Session ID: {}", session.getId());
        log.error("Error: ", exception);
        
        if (session.isOpen()) {
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (Exception e) {
                log.error("Error closing session after transport error", e);
            }
        }
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