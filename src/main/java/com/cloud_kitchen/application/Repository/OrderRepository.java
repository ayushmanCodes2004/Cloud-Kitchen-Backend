package com.cloud_kitchen.application.Repository;

import com.cloud_kitchen.application.Entity.Order;
import com.cloud_kitchen.application.Entity.OrderStatus;
import com.cloud_kitchen.application.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByStudent(Student student);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findByStudentAndStatus(Student student, OrderStatus status);
    List<Order> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByStudentOrderByCreatedAtDesc(Student student);


    @Query("SELECT DISTINCT o FROM Order o " +
            "JOIN FETCH o.student s " +              // Fetch student
            "LEFT JOIN FETCH o.orderItems oi " +     // Fetch order items
            "LEFT JOIN FETCH oi.menuItem mi " +      // Fetch menu items
            "LEFT JOIN FETCH mi.chef c " +           // Fetch chef
            "WHERE mi.chef.id = :chefId " +
            "ORDER BY o.createdAt DESC")
    List<Order> findOrdersByChefId(@Param("chefId") Long chefId);
}
