package com.cloud_kitchen.application.Service;

import com.cloud_kitchen.application.DTO.ChatMessageDto;
import com.cloud_kitchen.application.Entity.*;
import com.cloud_kitchen.application.Repository.ChatMessageRepository;
import com.cloud_kitchen.application.Repository.ChatSessionRepository;
import com.cloud_kitchen.application.Repository.OrderRepository;
import com.cloud_kitchen.application.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {
    
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    
    /**
     * Creates a chat session for an order when it reaches CONFIRMED status
     */
    @Transactional
    public void enableChatForOrder(Order order) {
        log.info("enableChatForOrder called for order ID: {}", order.getId());
        
        // Check if chat session already exists
        Optional<ChatSession> existingSession = chatSessionRepository.findByOrderId(order.getId());
        if (existingSession.isPresent()) {
            // If the existing session is inactive, reactivate it
            ChatSession session = existingSession.get();
            if (session.getStatus() != ChatSession.ChatStatus.ACTIVE) {
                log.info("Reactivating existing chat session for order {}", order.getId());
                session.setStatus(ChatSession.ChatStatus.ACTIVE);
                chatSessionRepository.save(session);
            } else {
                log.info("Chat session already exists and is active for order {}", order.getId());
            }
            return; // Chat session already exists
        }
        
        log.info("Creating new chat session for order {}", order.getId());
        
        // Create a new chat session
        ChatSession session = new ChatSession();
        session.setOrderId(order.getId());
        session.setCreatedByStudentId(order.getStudent().getId());
        
        // Load the order with its items to avoid lazy loading issues
        Order fullOrder = orderRepository.findById(order.getId())
            .orElseThrow(() -> new RuntimeException("Order not found: " + order.getId()));
        
        // Get the chef ID from the first order item (in case of multi-chef orders, 
        // we'll create separate chat sessions for each chef, but for simplicity we'll use the first chef)
        if (!fullOrder.getOrderItems().isEmpty()) {
            log.info("Order items size: {}", fullOrder.getOrderItems().size());
            Long chefId = fullOrder.getOrderItems().get(0).getMenuItem().getChef().getId();
            log.info("Chef ID found: {}", chefId);
            session.setAssignedChefId(chefId);
        } else {
            log.error("No order items found for order {}", order.getId());
            // If no order items, we might have a problem - throw an exception or handle gracefully
            throw new RuntimeException("Cannot create chat session for order without items: " + order.getId());
        }
        
        session.setStatus(ChatSession.ChatStatus.ACTIVE);
        
        ChatSession savedSession = chatSessionRepository.save(session);
        log.info("Chat session created with ID: {}", savedSession.getId());
        
        // Verify the session was saved properly
        Optional<ChatSession> verification = chatSessionRepository.findById(savedSession.getId());
        if (verification.isPresent() && verification.get().getStatus() == ChatSession.ChatStatus.ACTIVE) {
            log.info("Chat session verification successful for order {}", order.getId());
        } else {
            log.error("Chat session verification failed for order {}", order.getId());
        }
    }
    
    /**
     * Disables a chat session when order reaches DELIVERED status
     */
    @Transactional
    public void disableChatForOrder(Long orderId) {
        Optional<ChatSession> sessionOpt = chatSessionRepository.findByOrderId(orderId);
        if (sessionOpt.isPresent()) {
            ChatSession session = sessionOpt.get();
            session.setStatus(ChatSession.ChatStatus.INACTIVE);
            session.setEndedAt(java.time.LocalDateTime.now());
            chatSessionRepository.save(session);
        }
    }
    
    /**
     * Checks if chat is enabled for an order
     */
    public boolean isChatEnabledForOrder(Long orderId) {
        log.info("isChatEnabledForOrder called for order ID: {}", orderId);
        
        // Check if there's an active chat session
        Optional<ChatSession> session = chatSessionRepository.findActiveChatSessionByOrderId(orderId);
        
        if (session.isPresent()) {
            log.info("Found active chat session with ID: {}, status: {}", session.get().getId(), session.get().getStatus());
            boolean result = session.get().getStatus() == ChatSession.ChatStatus.ACTIVE;
            log.info("isChatEnabledForOrder result for order {}: {}", orderId, result);
            return result;
        }
        
        log.info("No active chat session found, checking order status...");
        
        // No active session exists, check if we should create one
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (!orderOpt.isPresent()) {
            log.warn("Order not found for ID: {}", orderId);
            return false;
        }
        
        Order order = orderOpt.get();
        log.info("Order found with status: {}", order.getStatus());
        
        // Check if order is in a state that allows chat
        if (order.getStatus() == OrderStatus.PENDING || order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            log.info("Order status {} does not allow chat", order.getStatus());
            return false;
        }
        
        log.info("Order status {} allows chat, checking for existing session...", order.getStatus());
        
        // Chat is allowed for CONFIRMED, PREPARING, and READY statuses
        // First check if a session already exists
        Optional<ChatSession> existingSession = chatSessionRepository.findByOrderId(orderId);
        if (existingSession.isPresent()) {
            // If session exists but is inactive, we might need to reactivate it
            ChatSession existingChatSession = existingSession.get();
            log.info("Found existing session with status: {}", existingChatSession.getStatus());
            if (existingChatSession.getStatus() != ChatSession.ChatStatus.ACTIVE) {
                log.info("Reactivating existing chat session for order {}", orderId);
                existingChatSession.setStatus(ChatSession.ChatStatus.ACTIVE);
                chatSessionRepository.save(existingChatSession);
            }
            return true;
        } else {
            log.info("No existing session found, creating chat session for order {} in status {}", orderId, order.getStatus());
            try {
                enableChatForOrder(order); // Create the session
                return true;
            } catch (Exception e) {
                log.error("Error creating chat session for order {}: {}", orderId, e.getMessage(), e);
                return false;
            }
        }
    }
    
    /**
     * Sends a message in a chat session (for WebSocket - with explicit userId)
     */
    @Transactional
    public ChatMessageDto sendMessage(Long orderId, Long userId, String message) {
        log.info("sendMessage called - orderId: {}, userId: {}, message: {}", orderId, userId, message);
        
        // Get the user
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Get the order with items eagerly loaded to avoid lazy loading issues
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        log.info("Order loaded with {} items", order.getOrderItems().size());
        
        // Check if chat is enabled for this order
        if (!isChatEnabledForOrder(orderId)) {
            throw new RuntimeException("Chat is not enabled for this order");
        }
        
        // Verify that the user is either the student who placed the order or the assigned chef
        boolean isAuthorized = order.getStudent().getId().equals(userId);
        log.info("Is student: {}", isAuthorized);
        
        if (!isAuthorized) {
            // Check if the user is the chef assigned to this order
            isAuthorized = order.getOrderItems().stream()
                    .anyMatch(item -> item.getMenuItem().getChef().getId().equals(userId));
            log.info("Is chef: {}", isAuthorized);
        }
        
        if (!isAuthorized) {
            log.error("User {} not authorized for order {}", userId, orderId);
            throw new RuntimeException("Unauthorized to send message for this order");
        }
        
        // Get the chat session
        ChatSession session = chatSessionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Chat session not found for order"));
        
        log.info("Creating chat message for session: {}", session.getId());
        
        // Create and save the message
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setChatSessionId(session.getId());
        chatMessage.setSenderUserId(userId);
        chatMessage.setMessage(message);
        chatMessage.setMessageType(ChatMessage.MessageType.TEXT);
        
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        log.info("Message saved with ID: {}", savedMessage.getId());
        
        // Get sender name
        String senderName = sender.getName();
        
        ChatMessageDto dto = ChatMessageDto.fromEntity(savedMessage, senderName);
        log.info("Returning ChatMessageDto: {}", dto);
        
        return dto;
    }
    
    /**
     * Sends a message in a chat session (for REST API - uses current authenticated user)
     */
    @Transactional
    public ChatMessageDto sendMessage(Long orderId, String message) {
        // Verify that the current user is authorized to send a message
        User currentUser = authService.getCurrentUser();
        
        // Delegate to the method that accepts userId
        return sendMessage(orderId, currentUser.getId(), message);
    }
    
    /**
     * Gets all messages for a chat session
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDto> getChatMessages(Long orderId) {
        // Verify that the current user is authorized to view messages
        User currentUser = authService.getCurrentUser();
        
        // Get the order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Verify that the user is either the student who placed the order or the assigned chef
        boolean isAuthorized = order.getStudent().getId().equals(currentUser.getId());
        if (!isAuthorized) {
            // Check if the user is the chef assigned to this order
            isAuthorized = order.getOrderItems().stream()
                    .anyMatch(item -> item.getMenuItem().getChef().getId().equals(currentUser.getId()));
        }
        
        if (!isAuthorized) {
            throw new RuntimeException("Unauthorized to view messages for this order");
        }
        
        // Get the chat session
        ChatSession session = chatSessionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Chat session not found for order"));
        
        // Get all messages for this session
        List<ChatMessage> messages = chatMessageRepository.findByChatSessionIdOrderBySentAtAsc(session.getId());
        
        // Convert to DTOs with sender names
        return messages.stream().map(msg -> {
            User sender = userRepository.findById(msg.getSenderUserId())
                    .orElseThrow(() -> new RuntimeException("Sender not found"));
            return ChatMessageDto.fromEntity(msg, sender.getName());
        }).collect(Collectors.toList());
    }
    
    /**
     * Gets active chat sessions for a user (either as student or chef)
     */
    @Transactional(readOnly = true)
    public List<ChatSession> getActiveChatSessionsForUser() {
        User currentUser = authService.getCurrentUser();
        
        // Get all chat sessions where the user is either the student or the chef
        List<ChatSession> sessions = chatSessionRepository.findByCreatedByStudentIdOrAssignedChefId(
                currentUser.getId(), currentUser.getId());
        
        // Filter to only active sessions
        return sessions.stream()
                .filter(session -> session.getStatus() == ChatSession.ChatStatus.ACTIVE)
                .collect(Collectors.toList());
    }
    
    /**
     * Checks if a user is authorized to participate in a chat for an order
     */
    public boolean isUserAuthorizedForChat(Long orderId, Long userId) {
        // Get the order with items eagerly loaded
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        // Check if user is the student who placed the order
        if (order.getStudent().getId().equals(userId)) {
            return true;
        }
        
        // Check if user is the chef assigned to this order
        return order.getOrderItems().stream()
                .anyMatch(item -> item.getMenuItem().getChef().getId().equals(userId));
    }
}