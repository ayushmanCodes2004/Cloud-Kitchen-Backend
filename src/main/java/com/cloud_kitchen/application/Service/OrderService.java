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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final StudentRepository studentRepository;
    private final AuthService authService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        User currentUser = authService.getCurrentUser();

        Student student = studentRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setStudent(student);
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setSpecialInstructions(request.getSpecialInstructions());
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;
        int maxPreparationTime = 0;

        for (OrderItemRequest itemRequest : request.getItems()) {
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found with id: " + itemRequest.getMenuItemId()));

            if (!menuItem.getAvailable()) {
                throw new RuntimeException("Menu item " + menuItem.getName() + " is not available");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(menuItem.getPrice());
            orderItem.setSubtotal(menuItem.getPrice() * itemRequest.getQuantity());

            orderItems.add(orderItem);
            totalAmount += orderItem.getSubtotal();

            if (menuItem.getPreparationTime() != null && menuItem.getPreparationTime() > maxPreparationTime) {
                maxPreparationTime = menuItem.getPreparationTime();
            }
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setEstimatedDeliveryTime(LocalDateTime.now().plusMinutes(maxPreparationTime + 30));

        Order savedOrder = orderRepository.save(order);

        // Convert to DTO to avoid lazy loading issues
        return convertToOrderResponse(savedOrder);
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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return convertToOrderResponse(updatedOrder);
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
            throw new RuntimeException("Cannot cancel order in " + order.getStatus() + " status");
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

    // Convert OrderItem entity to OrderItemResponse DTO
    private OrderItemResponse convertToOrderItemResponse(OrderItem orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(orderItem.getId());
        response.setMenuItemId(orderItem.getMenuItem().getId());
        response.setMenuItemName(orderItem.getMenuItem().getName());
        response.setQuantity(orderItem.getQuantity());
        response.setPrice(orderItem.getPrice());
        response.setSubtotal(orderItem.getSubtotal());
        return response;
    }
}

