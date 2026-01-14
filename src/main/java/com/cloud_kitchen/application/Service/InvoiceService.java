package com.cloud_kitchen.application.Service;

import com.cloud_kitchen.application.DTO.InvoiceItemDTO;
import com.cloud_kitchen.application.DTO.InvoiceResponse;
import com.cloud_kitchen.application.Entity.Order;
import com.cloud_kitchen.application.Entity.OrderItem;
import com.cloud_kitchen.application.Repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InvoiceService {

    @Autowired
    private OrderRepository orderRepository;

    public InvoiceResponse generateInvoice(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        InvoiceResponse invoice = new InvoiceResponse();
        
        // Invoice details
        invoice.setInvoiceNumber(order.getInvoiceNumber());
        invoice.setOrderNumber(order.getOrderNumber());
        invoice.setInvoiceDate(order.getCreatedAt());
        invoice.setOrderDate(order.getCreatedAt());
        
        // Customer details
        invoice.setCustomerName(order.getStudent().getName());
        invoice.setCustomerEmail(order.getStudent().getEmail());
        invoice.setCustomerPhone(order.getStudent().getPhoneNumber());
        invoice.setDeliveryAddress(order.getDeliveryAddress());
        
        // Order items
        List<InvoiceItemDTO> items = order.getOrderItems().stream()
                .map(this::mapToInvoiceItem)
                .collect(Collectors.toList());
        invoice.setItems(items);
        
        // Calculate amounts
        Double subtotal = order.getOrderItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        
        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(order.getTaxAmount());
        invoice.setPlatformFee(order.getPlatformFee());
        invoice.setTotalAmount(order.getTotalAmount());
        
        // Payment details
        invoice.setPaymentMethod(order.getPaymentMethod().toString());
        invoice.setPaymentStatus(order.getPaymentStatus().toString());
        
        // Additional info
        invoice.setSpecialInstructions(order.getSpecialInstructions());
        invoice.setStatus(order.getStatus().toString());
        
        return invoice;
    }

    private InvoiceItemDTO mapToInvoiceItem(OrderItem orderItem) {
        InvoiceItemDTO dto = new InvoiceItemDTO();
        dto.setItemName(orderItem.getMenuItem().getName());
        dto.setQuantity(orderItem.getQuantity());
        dto.setUnitPrice(orderItem.getPrice());
        dto.setTotalPrice(orderItem.getPrice() * orderItem.getQuantity());
        dto.setChefName(orderItem.getMenuItem().getChef().getName());
        return dto;
    }

    public String generateInvoiceNumber() {
        // Generate invoice number: INV-YYYYMMDD-XXXXX
        String datePart = java.time.LocalDate.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = orderRepository.count() + 1;
        return String.format("INV-%s-%05d", datePart, count);
    }
}
