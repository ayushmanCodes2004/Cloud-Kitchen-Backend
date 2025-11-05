package com.cloud_kitchen.application.Service;

import com.cloud_kitchen.application.DTO.ChefRatingStats;
import com.cloud_kitchen.application.DTO.MenuItemRatingStats;
import com.cloud_kitchen.application.DTO.RatingRequest;
import com.cloud_kitchen.application.DTO.RatingResponse;
import com.cloud_kitchen.application.Entity.Chef;
import com.cloud_kitchen.application.Entity.MenuItem;
import com.cloud_kitchen.application.Entity.Order;
import com.cloud_kitchen.application.Entity.Rating;
import com.cloud_kitchen.application.Entity.Student;
import com.cloud_kitchen.application.Repository.ChefRepository;
import com.cloud_kitchen.application.Repository.MenuItemRepository;
import com.cloud_kitchen.application.Repository.OrderRepository;
import com.cloud_kitchen.application.Repository.RatingRepository;
import com.cloud_kitchen.application.Repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final StudentRepository studentRepository;
    private final ChefRepository chefRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public RatingResponse rateChef(Long studentId, RatingRequest request) {
        // Check if student already rated this specific order
        if (ratingRepository.existsByStudentIdAndChefIdAndOrderId(studentId, request.getChefId(), request.getOrderId())) {
            throw new RuntimeException("You have already rated this order");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Chef chef = chefRepository.findById(request.getChefId())
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Verify the order belongs to the student
        if (!order.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("You can only rate your own orders");
        }

        // Verify the order is delivered
        if (!order.getStatus().toString().equals("DELIVERED")) {
            throw new RuntimeException("You can only rate delivered orders");
        }

        Rating rating = new Rating();
        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        rating.setStudent(student);
        rating.setChef(chef);
        rating.setOrder(order);

        Rating savedRating = ratingRepository.save(rating);

        return mapToRatingResponse(savedRating);
    }

    @Transactional(readOnly = true)
    public ChefRatingStats getChefRatings(Long chefId) {
        Chef chef = chefRepository.findById(chefId)
                .orElseThrow(() -> new RuntimeException("Chef not found"));

        List<Rating> ratings = ratingRepository.findByChefIdOrderByCreatedAtDesc(chefId);
        Double averageRating = ratingRepository.findAverageRatingByChefId(chefId);
        Long totalRatings = ratingRepository.countRatingsByChefId(chefId);

        List<RatingResponse> ratingResponses = ratings.stream()
                .map(this::mapToRatingResponse)
                .collect(Collectors.toList());

        ChefRatingStats stats = new ChefRatingStats();
        stats.setChefId(chefId);
        stats.setChefName(chef.getName());
        stats.setAverageRating(averageRating != null ? averageRating : 0.0);
        stats.setTotalRatings(totalRatings);
        stats.setRatings(ratingResponses);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<ChefRatingStats> getAllChefRatings() {
        List<Chef> chefs = chefRepository.findAll();
        return chefs.stream()
                .map(chef -> getChefRatings(chef.getId()))
                .collect(Collectors.toList());
    }

    @Transactional
    public RatingResponse rateMenuItem(Long studentId, RatingRequest request) {
        // Check if student already rated this menu item for this specific order
        if (ratingRepository.existsByStudentIdAndMenuItemIdAndOrderId(studentId, request.getMenuItemId(), request.getOrderId())) {
            throw new RuntimeException("You have already rated this menu item for this order");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Verify the order belongs to the student
        if (!order.getStudent().getId().equals(studentId)) {
            throw new RuntimeException("You can only rate your own orders");
        }

        // Verify the order is delivered
        if (!order.getStatus().toString().equals("DELIVERED")) {
            throw new RuntimeException("You can only rate delivered orders");
        }

        Rating rating = new Rating();
        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        rating.setStudent(student);
        rating.setMenuItem(menuItem);
        rating.setOrder(order);

        Rating savedRating = ratingRepository.save(rating);

        return mapToRatingResponse(savedRating);
    }

    @Transactional(readOnly = true)
    public MenuItemRatingStats getMenuItemRatings(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        List<Rating> ratings = ratingRepository.findByMenuItemIdOrderByCreatedAtDesc(menuItemId);
        Double averageRating = ratingRepository.findAverageRatingByMenuItemId(menuItemId);
        Long totalRatings = ratingRepository.countRatingsByMenuItemId(menuItemId);

        List<RatingResponse> ratingResponses = ratings.stream()
                .map(this::mapToRatingResponse)
                .collect(Collectors.toList());

        MenuItemRatingStats stats = new MenuItemRatingStats();
        stats.setMenuItemId(menuItemId);
        stats.setMenuItemName(menuItem.getName());
        stats.setAverageRating(averageRating != null ? averageRating : 0.0);
        stats.setTotalRatings(totalRatings);
        stats.setRatings(ratingResponses);

        return stats;
    }

    @Transactional(readOnly = true)
    public List<MenuItemRatingStats> getAllMenuItemRatings() {
        List<MenuItem> menuItems = menuItemRepository.findAll();
        return menuItems.stream()
                .map(menuItem -> getMenuItemRatings(menuItem.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Long> getRatedOrderIdsByStudent(Long studentId) {
        return ratingRepository.findRatedOrderIdsByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public List<String> getRatedMenuItemsByStudent(Long studentId) {
        return ratingRepository.findRatedMenuItemsByStudentId(studentId);
    }

    private RatingResponse mapToRatingResponse(Rating rating) {
        RatingResponse response = new RatingResponse();
        response.setId(rating.getId());
        response.setRating(rating.getRating());
        response.setComment(rating.getComment());
        response.setStudentName(rating.getStudent().getName());
        response.setCreatedAt(rating.getCreatedAt());
        return response;
    }
}