package com.cloud_kitchen.application.Controller;

import com.cloud_kitchen.application.DTO.ApiResponse;
import com.cloud_kitchen.application.DTO.OrderRequest;
import com.cloud_kitchen.application.DTO.OrderResponse;
import com.cloud_kitchen.application.Entity.Order;
import com.cloud_kitchen.application.Entity.OrderStatus;
import com.cloud_kitchen.application.Service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@RestController
//@RequestMapping("/api/orders")
//@RequiredArgsConstructor
//public class OrderController {
//
//    private final OrderService orderService;
//
//    @PostMapping
//    @PreAuthorize("hasRole('STUDENT')")
//    public ResponseEntity<ApiResponse> createOrder(@Valid @RequestBody OrderRequest request) {
//        try {
//            Order order = orderService.createOrder(request);
//            return ResponseEntity.status(HttpStatus.CREATED)
//                    .body(new ApiResponse(true, "Order created successfully", order));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ApiResponse(false, e.getMessage()));
//        }
//    }
//
//    @GetMapping("/my-orders")
//    @PreAuthorize("hasRole('STUDENT')")
//    public ResponseEntity<ApiResponse> getMyOrders() {
//        try {
//            List<Order> orders = orderService.getMyOrders();
//            return ResponseEntity.ok(new ApiResponse(true, "Orders fetched successfully", orders));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse(false, e.getMessage()));
//        }
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponse> getOrderById(@PathVariable Long id) {
//        try {
//            Order order = orderService.getOrderById(id);
//            return ResponseEntity.ok(new ApiResponse(true, "Order fetched successfully", order));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND)
//                    .body(new ApiResponse(false, e.getMessage()));
//        }
//    }
//
//    @GetMapping
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<ApiResponse> getAllOrders() {
//        try {
//            List<Order> orders = orderService.getAllOrders();
//            return ResponseEntity.ok(new ApiResponse(true, "Orders fetched successfully", orders));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse(false, e.getMessage()));
//        }
//    }
//
//    @GetMapping("/status/{status}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<ApiResponse> getOrdersByStatus(@PathVariable OrderStatus status) {
//        try {
//            List<Order> orders = orderService.getOrdersByStatus(status);
//            return ResponseEntity.ok(new ApiResponse(true, "Orders fetched successfully", orders));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(new ApiResponse(false, e.getMessage()));
//        }
//    }
//
//    @PatchMapping("/{id}/status")
//    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF')")
//    public ResponseEntity<ApiResponse> updateOrderStatus(@PathVariable Long id,
//                                                         @RequestParam OrderStatus status) {
//        try {
//            Order order = orderService.updateOrderStatus(id, status);
//            return ResponseEntity.ok(new ApiResponse(true, "Order status updated successfully", order));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ApiResponse(false, e.getMessage()));
//        }
//    }
//
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('STUDENT')")
//    public ResponseEntity<ApiResponse> cancelOrder(@PathVariable Long id) {
//        try {
//            orderService.cancelOrder(id);
//            return ResponseEntity.ok(new ApiResponse(true, "Order cancelled successfully"));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ApiResponse(false, e.getMessage()));
//        }
//    }
//}


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        try {
            OrderResponse order = orderService.createOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "Order created successfully", order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse> getMyOrders() {
        try {
            List<OrderResponse> orders = orderService.getMyOrders();
            return ResponseEntity.ok(new ApiResponse(true, "Orders fetched successfully", orders));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable Long id) {
        try {
            OrderResponse order = orderService.getOrderById(id);
            return ResponseEntity.ok(new ApiResponse(true, "Order fetched successfully", order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAllOrders() {
        try {
            List<OrderResponse> orders = orderService.getAllOrders();
            return ResponseEntity.ok(new ApiResponse(true, "Orders fetched successfully", orders));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getOrdersByStatus(@PathVariable OrderStatus status) {
        try {
            List<OrderResponse> orders = orderService.getOrdersByStatus(status);
            return ResponseEntity.ok(new ApiResponse(true, "Orders fetched successfully", orders));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'CHEF')")
    public ResponseEntity<ApiResponse> updateOrderStatus(@PathVariable Long id,
                                                         @RequestParam OrderStatus status) {
        try {
            OrderResponse order = orderService.updateOrderStatus(id, status);
            return ResponseEntity.ok(new ApiResponse(true, "Order status updated successfully", order));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse> cancelOrder(@PathVariable Long id) {
        try {
            orderService.cancelOrder(id);
            return ResponseEntity.ok(new ApiResponse(true, "Order cancelled successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        }
    }
}


