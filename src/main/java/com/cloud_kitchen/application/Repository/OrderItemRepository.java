package com.cloud_kitchen.application.Repository;

import com.cloud_kitchen.application.Entity.Order;
import com.cloud_kitchen.application.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);
}
