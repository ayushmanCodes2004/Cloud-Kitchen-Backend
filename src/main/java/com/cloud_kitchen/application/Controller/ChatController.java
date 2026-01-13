package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.ApiResponse;
import com.cloud_kitchen.application.DTO.ChatMessageDto;
import com.cloud_kitchen.application.Entity.ChatSession;
import com.cloud_kitchen.application.Entity.Order;
import com.cloud_kitchen.application.Entity.OrderItem;
import com.cloud_kitchen.application.Entity.User;
import com.cloud_kitchen.application.Repository.ChatSessionRepository;
import com.cloud_kitchen.application.Repository.OrderRepository;
import com.cloud_kitchen.application.Service.AuthService;
import com.cloud_kitchen.application.Service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    
    private final ChatService chatService;
    private final AuthService authService;
    private final OrderRepository orderRepository;
    private final ChatSessionRepository chatSessionRepository;
    
    @GetMapping("/order/{orderId}/messages")
    @PreAuthorize("hasAnyRole('STUDENT', 'CHEF')")
    public ResponseEntity<ApiResponse<List<ChatMessageDto>>> getOrderMessages(@PathVariable Long orderId) {
        try {
            List<ChatMessageDto> messages = chatService.getChatMessages(orderId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Messages fetched successfully", messages));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @GetMapping("/my-active-sessions")
    @PreAuthorize("hasAnyRole('STUDENT', 'CHEF')")
    public ResponseEntity<ApiResponse<List<ChatSession>>> getActiveChatSessions() {
        try {
            List<ChatSession> sessions = chatService.getActiveChatSessionsForUser();
            return ResponseEntity.ok(new ApiResponse<>(true, "Active chat sessions fetched successfully", sessions));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }
    
    @GetMapping("/order/{orderId}/enabled")
    @PreAuthorize("hasAnyRole('STUDENT', 'CHEF')")
    public ResponseEntity<ApiResponse<Boolean>> isChatEnabledForOrder(@PathVariable Long orderId) {
        log.info("ChatController.isChatEnabledForOrder called for order ID: {}", orderId);
        try {
            boolean isEnabled = chatService.isChatEnabledForOrder(orderId);
            log.info("ChatController returning {} for order ID: {}", isEnabled, orderId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Chat status checked successfully", isEnabled));
        } catch (Exception e) {
            log.error("Exception in ChatController.isChatEnabledForOrder: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), false));
        }
    }
    @GetMapping("/order/{orderId}/debug")
    @PreAuthorize("hasAnyRole('STUDENT', 'CHEF')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debugChatForOrder(@PathVariable Long orderId) {
        log.info("ChatController.debugChatForOrder called for order ID: {}", orderId);
        try {
            Map<String, Object> debugInfo = new HashMap<>();
            
            // Get current user
            User currentUser = authService.getCurrentUser();
            debugInfo.put("currentUserId", currentUser.getId());
            debugInfo.put("currentUserName", currentUser.getName());
            debugInfo.put("currentUserRole", currentUser.getRole());
            
            // Get order info
            Optional<Order> orderOpt = orderRepository.findById(orderId);
            if (!orderOpt.isPresent()) {
                debugInfo.put("error", "Order not found");
                return ResponseEntity.ok(new ApiResponse<>(true, "Debug info", debugInfo));
            }
            
            Order order = orderOpt.get();
            debugInfo.put("orderStatus", order.getStatus());
            debugInfo.put("orderStudentId", order.getStudent().getId());
            debugInfo.put("orderStudentName", order.getStudent().getName());
            debugInfo.put("orderItemsCount", order.getOrderItems().size());
            
            // Check order items and chefs
            List<Map<String, Object>> itemsInfo = new ArrayList<>();
            for (OrderItem item : order.getOrderItems()) {
                Map<String, Object> itemInfo = new HashMap<>();
                itemInfo.put("menuItemId", item.getMenuItem().getId());
                itemInfo.put("menuItemName", item.getMenuItem().getName());
                itemInfo.put("chefId", item.getMenuItem().getChef().getId());
                itemInfo.put("chefName", item.getMenuItem().getChef().getName());
                itemsInfo.add(itemInfo);
            }
            debugInfo.put("orderItems", itemsInfo);
            
            // Check authorization
            boolean isStudent = order.getStudent().getId().equals(currentUser.getId());
            boolean isChef = order.getOrderItems().stream()
                    .anyMatch(item -> item.getMenuItem().getChef().getId().equals(currentUser.getId()));
            debugInfo.put("isAuthorizedAsStudent", isStudent);
            debugInfo.put("isAuthorizedAsChef", isChef);
            debugInfo.put("isAuthorized", isStudent || isChef);
            
            // Check existing chat session
            Optional<ChatSession> existingSession = chatSessionRepository.findByOrderId(orderId);
            if (existingSession.isPresent()) {
                ChatSession session = existingSession.get();
                debugInfo.put("existingSessionId", session.getId());
                debugInfo.put("existingSessionStatus", session.getStatus());
                debugInfo.put("existingSessionStudentId", session.getCreatedByStudentId());
                debugInfo.put("existingSessionChefId", session.getAssignedChefId());
            } else {
                debugInfo.put("existingSession", "None");
            }
            
            // Check active chat session
            Optional<ChatSession> activeSession = chatSessionRepository.findActiveChatSessionByOrderId(orderId);
            debugInfo.put("hasActiveSession", activeSession.isPresent());
            
            // Try to check if chat is enabled
            try {
                boolean isChatEnabled = chatService.isChatEnabledForOrder(orderId);
                debugInfo.put("isChatEnabled", isChatEnabled);
            } catch (Exception e) {
                debugInfo.put("chatEnabledError", e.getMessage());
            }
            
            return ResponseEntity.ok(new ApiResponse<>(true, "Debug info retrieved", debugInfo));
            
        } catch (Exception e) {
            log.error("Exception in ChatController.debugChatForOrder: {}", e.getMessage(), e);
            Map<String, Object> errorInfo = new HashMap<>();
            errorInfo.put("error", e.getMessage());
            return ResponseEntity.ok(new ApiResponse<>(true, "Debug info with error", errorInfo));
        }
    }
}