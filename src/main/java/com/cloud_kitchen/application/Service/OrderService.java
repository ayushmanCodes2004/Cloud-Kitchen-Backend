package com.cloud_kitchen.application.Service;

import com.cloud_kitchen.application.DTO.OrderItemRequest;
import com.cloud_kitchen.application.DTO.OrderItemResponse;
import com.cloud_kitchen.application.DTO.OrderRequest;
import com.cloud_kitchen.application.DTO.OrderResponse;
import com.cloud_kitchen.application.Entity.*;
import com.cloud_kitchen.application.Repository.MenuItemRepository;
import com.cloud_kitchen.application.Repository.OrderItemRepository;
import com.cloud_kitchen.application.Repository.OrderRepository;
import com.cloud_kitchen.application.Repository.StudentRepository;
import com.cloud_kitchen.application.Service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

//package com.cloud_kitchen.application.Service;
//
//import com.cloud_kitchen.application.DTO.OrderItemRequest;
//import com.cloud_kitchen.application.DTO.OrderRequest;
//import com.cloud_kitchen.application.Entity.*;
//import com.cloud_kitchen.application.Repository.MenuItemRepository;
//import com.cloud_kitchen.application.Repository.OrderItemRepository;
//import com.cloud_kitchen.application.Repository.OrderRepository;
//import com.cloud_kitchen.application.Repository.StudentRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class OrderService {
//
//    private final OrderRepository orderRepository;
//    private final OrderItemRepository orderItemRepository;
//    private final MenuItemRepository menuItemRepository;
//    private final StudentRepository studentRepository;
//    private final AuthService authService;
//
//    @Transactional
//    public Order createOrder(OrderRequest request) {
//        User currentUser = authService.getCurrentUser();
//
//        Student student = studentRepository.findById(currentUser.getId())
//                .orElseThrow(() -> new RuntimeException("Student not found"));
//
//        Order order = new Order();
//        order.setOrderNumber(generateOrderNumber());
//        order.setStudent(student);
//        order.setDeliveryAddress(request.getDeliveryAddress());
//        order.setSpecialInstructions(request.getSpecialInstructions());
//        order.setStatus(OrderStatus.PENDING);
//
//        List<OrderItem> orderItems = new ArrayList<>();
//        double totalAmount = 0.0;
//        int maxPreparationTime = 0;
//
//        for (OrderItemRequest itemRequest : request.getItems()) {
//            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
//                    .orElseThrow(() -> new RuntimeException("Menu item not found"));
//
//            if (!menuItem.getAvailable()) {
//                throw new RuntimeException("Menu item " + menuItem.getName() + " is not available");
//            }
//
//            OrderItem orderItem = new OrderItem();
//            orderItem.setOrder(order);
//            orderItem.setMenuItem(menuItem);
//            orderItem.setQuantity(itemRequest.getQuantity());
//            orderItem.setPrice(menuItem.getPrice());
//            orderItem.setSubtotal(menuItem.getPrice() * itemRequest.getQuantity());
//
//            orderItems.add(orderItem);
//            totalAmount += orderItem.getSubtotal();
//
//            if (menuItem.getPreparationTime() != null && menuItem.getPreparationTime() > maxPreparationTime) {
//                maxPreparationTime = menuItem.getPreparationTime();
//            }
//        }
//
//        order.setOrderItems(orderItems);
//        order.setTotalAmount(totalAmount);
//        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(maxPreparationTime + 30));
//
//        return orderRepository.save(order);
//    }
//
//    public Order getOrderById(Long id) {
//        Order order = orderRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        User currentUser = authService.getCurrentUser();
//
//        if (!order.getStudent().getId().equals(currentUser.getId()) &&
//                !currentUser.getRole().name().equals("ADMIN")) {
//            throw new RuntimeException("Access denied");
//        }
//
//        return order;
//    }
//
//    public List<Order> getMyOrders() {
//        User currentUser = authService.getCurrentUser();
//        Student student = studentRepository.findById(currentUser.getId())
//                .orElseThrow(() -> new RuntimeException("Student not found"));
//
//        return orderRepository.findByStudentOrderByCreatedAtDesc(student);
//    }
//
//    public List<Order> getAllOrders() {
//        return orderRepository.findAll();
//    }
//
//    public List<Order> getOrdersByStatus(OrderStatus status) {
//        return orderRepository.findByStatus(status);
//    }
//
//    @Transactional
//    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        order.setStatus(newStatus);
//        return orderRepository.save(order);
//    }
//
//    @Transactional
//    public void cancelOrder(Long orderId) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Order not found"));
//
//        User currentUser = authService.getCurrentUser();
//
//        if (!order.getStudent().getId().equals(currentUser.getId())) {
//            throw new RuntimeException("You can only cancel your own orders");
//        }
//
//        if (order.getStatus() != OrderStatus.PENDING) {
//            throw new RuntimeException("Cannot cancel order in " + order.getStatus() + " status");
//        }
//
//        order.setStatus(OrderStatus.CANCELLED);
//        orderRepository.save(order);
//    }
//
//    private String generateOrderNumber() {
//        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
//    }
//}
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final StudentRepository studentRepository;
    private final AuthService authService;
    private final ChatService chatService;

//    @Transactional
//    public OrderResponse createOrder(OrderRequest request) {
//        User currentUser = authService.getCurrentUser();
//
//        Student student = studentRepository.findById(currentUser.getId())
//                .orElseThrow(() -> new RuntimeException("Student not found"));
//
//        Order order = new Order();
//        order.setOrderNumber(generateOrderNumber());
//        order.setStudent(student);
//        order.setDeliveryAddress(request.getDeliveryAddress());
//        order.setSpecialInstructions(request.getSpecialInstructions());
//        order.setStatus(OrderStatus.PENDING);
//
//        List<OrderItem> orderItems = new ArrayList<>();
//        double totalAmount = 0.0;
//        int maxPreparationTime = 0;
//
//        for (OrderItemRequest itemRequest : request.getItems()) {
//            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
//                    .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + itemRequest.getMenuItemId()));
//
//            if (!menuItem.getAvailable()) {
//                throw new RuntimeException("Menu item " + menuItem.getName() + " is not available");
//            }
//
//            OrderItem orderItem = new OrderItem();
//            orderItem.setOrder(order);
//            orderItem.setMenuItem(menuItem);
//            orderItem.setQuantity(itemRequest.getQuantity());
//            orderItem.setPrice(menuItem.getPrice());
//            orderItem.setSubtotal(menuItem.getPrice() * itemRequest.getQuantity());
//
//            orderItems.add(orderItem);
//            totalAmount += orderItem.getSubtotal();
//
//            if (menuItem.getPreparationTime() != null && menuItem.getPreparationTime() > maxPreparationTime) {
//                maxPreparationTime = menuItem.getPreparationTime();
//            }
//        }
//
//        order.setOrderItems(orderItems);
//        order.setTotalAmount(totalAmount);
//        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(maxPreparationTime + 30));
//
//        Order savedOrder = orderRepository.save(order);
//
//        // Convert to DTO to avoid lazy loading issues
//        return convertToOrderResponse(savedOrder);
//    }

    @Transactional
    public List<OrderResponse> createOrder(OrderRequest request) {
        User currentUser = authService.getCurrentUser();
        Student student = studentRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Group items by chef
        Map<Long, List<OrderItemRequest>> itemsByChef = request.getItems().stream()
                .collect(Collectors.groupingBy(item -> {
                    MenuItem menuItem = menuItemRepository.findById(item.getMenuItemId())
                            .orElseThrow();
                    return menuItem.getChef().getId();
                }));

        List<OrderResponse> createdOrders = new ArrayList<>();
        String baseOrderNumber = generateOrderNumber();
        int subOrderIndex = 0;

        // Create separate order for each chef
        for (Map.Entry<Long, List<OrderItemRequest>> entry : itemsByChef.entrySet()) {
            Long chefId = entry.getKey();
            List<OrderItemRequest> chefItems = entry.getValue();

            Order order = new Order();
            order.setOrderNumber(baseOrderNumber + "-" + (char)('A' + subOrderIndex++));
            order.setStudent(student);
            
            // Use student's registered address if no delivery address provided or if it's the default value
            String deliveryAddress = request.getDeliveryAddress();
            if (deliveryAddress == null || deliveryAddress.trim().isEmpty() || deliveryAddress.equals("Student Hostel")) {
                // Build address from student's profile
                StringBuilder addressBuilder = new StringBuilder();
                if (student.getHostelName() != null && !student.getHostelName().trim().isEmpty()) {
                    addressBuilder.append(student.getHostelName());
                    if (student.getRoomNumber() != null && !student.getRoomNumber().trim().isEmpty()) {
                        addressBuilder.append(", Room ").append(student.getRoomNumber());
                    }
                }
                if (student.getAddress() != null && !student.getAddress().trim().isEmpty()) {
                    if (addressBuilder.length() > 0) {
                        addressBuilder.append(", ");
                    }
                    addressBuilder.append(student.getAddress());
                }
                if (addressBuilder.length() > 0) {
                    deliveryAddress = addressBuilder.toString();
                } else {
                    deliveryAddress = student.getCollege(); // Fallback to college name
                }
            }
            
            order.setDeliveryAddress(deliveryAddress);
            order.setSpecialInstructions(request.getSpecialInstructions());
            order.setStatus(OrderStatus.PENDING);

            List<OrderItem> orderItems = new ArrayList<>();
            double totalAmount = 0.0;

            for (OrderItemRequest itemRequest : chefItems) {
                MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                        .orElseThrow();

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setMenuItem(menuItem);
                orderItem.setQuantity(itemRequest.getQuantity());
                orderItem.setPrice(menuItem.getPrice());
                orderItem.setSubtotal(menuItem.getPrice() * itemRequest.getQuantity());

                orderItems.add(orderItem);
                totalAmount += orderItem.getSubtotal();
            }

            order.setOrderItems(orderItems);
            order.setTotalAmount(totalAmount);
            order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(30));

            Order savedOrder = orderRepository.save(order);
            createdOrders.add(convertToOrderResponse(savedOrder));
        }

        return createdOrders;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        User currentUser = authService.getCurrentUser();

        if (!order.getStudent().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Access denied");
        }

        return convertToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {
        User currentUser = authService.getCurrentUser();
        Student student = studentRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<Order> orders = orderRepository.findByStudentOrderByCreatedAtDesc(student);
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        // Load the complete order with items to avoid lazy loading issues when creating chat
        Optional<Order> orderOptional = orderRepository.findByIdWithItems(orderId);
        if (!orderOptional.isPresent()) {
            System.out.println("DEBUG: Order not found with ID: " + orderId);
            throw new RuntimeException("Order not found");
        }
        Order order = orderOptional.get();
        
        OrderStatus oldStatus = order.getStatus();

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        
        // Handle chat session activation/deactivation based on order status
        handleChatActivation(updatedOrder, oldStatus, newStatus);
        
        return convertToOrderResponse(updatedOrder);
    }
    
    private void handleChatActivation(Order order, OrderStatus oldStatus, OrderStatus newStatus) {
        log.info("handleChatActivation - Order ID: {}, Old status: {}, New status: {}", order.getId(), oldStatus, newStatus);
        
        // Enable chat ONLY when order reaches CONFIRMED status
        boolean shouldEnableChat = (newStatus == OrderStatus.CONFIRMED && oldStatus != OrderStatus.CONFIRMED);
        log.info("Should enable chat: {}, New status is CONFIRMED: {}, Old status was not CONFIRMED: {}", 
                shouldEnableChat, (newStatus == OrderStatus.CONFIRMED), (oldStatus != OrderStatus.CONFIRMED));
        
        if (shouldEnableChat) {
            log.info("Enabling chat for order {} - order reached CONFIRMED status", order.getId());
            chatService.enableChatForOrder(order);
        }
        
        // Disable chat when order is delivered or cancelled
        boolean shouldDisableChat = ((newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED) && oldStatus != newStatus);
        log.info("Should disable chat: {}, New status: {}", shouldDisableChat, newStatus);
        
        if (shouldDisableChat) {
            log.info("Disabling chat for order {} - order reached {} status", order.getId(), newStatus);
            chatService.disableChatForOrder(order.getId());
        }
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        User currentUser = authService.getCurrentUser();

        if (!order.getStudent().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only cancel your own orders");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Cannot cancel order in " + order.getStatus() + " status. Orders can only be cancelled while PENDING.");
        }

        // Students can cancel anytime while order is PENDING
        // Once chef confirms, cancellation is not allowed
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrderByChef(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        User currentUser = authService.getCurrentUser();

        // Verify the chef owns at least one item in this order
        boolean hasChefItems = order.getOrderItems().stream()
                .anyMatch(item -> item.getMenuItem().getChef().getId().equals(currentUser.getId()));

        if (!hasChefItems) {
            throw new RuntimeException("You can only cancel orders containing your menu items");
        }

        // Chefs can only cancel PENDING or CONFIRMED orders (before preparation starts)
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("Cannot cancel order in " + order.getStatus() + " status. Orders can only be cancelled before preparation starts.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderResponse convertToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStudentId(order.getStudent().getId());
        response.setStudentName(order.getStudent().getName());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setSpecialInstructions(order.getSpecialInstructions());
        response.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // Convert order items
        List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                .map(this::convertToOrderItemResponse)
                .collect(Collectors.toList());
        response.setOrderItems(orderItemResponses);

        return response;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByChef(Long chefId) {
        // Get all orders that contain at least one menu item from this chef
        List<Order> orders = orderRepository.findOrdersByChefId(chefId);
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }



    // Convert OrderItem entity to OrderItemResponse DTO
    private OrderItemResponse convertToOrderItemResponse(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(orderItem.getId());
        response.setMenuItemId(orderItem.getMenuItem().getId());
        response.setMenuItemName(orderItem.getMenuItem().getName());
        response.setQuantity(orderItem.getQuantity());
        response.setPrice(orderItem.getPrice());
        response.setSubtotal(orderItem.getSubtotal());
        response.setVegetarian(orderItem.getMenuItem().getVegetarian());// Added for veg/non-veg icon display

        response.setChefId(orderItem.getMenuItem().getChef().getId());
        response.setChefName(orderItem.getMenuItem().getChef().getName());
        return response;
    }
}

