package com.cloud_kitchen.application.Repository;

import com.cloud_kitchen.application.Entity.MenuItem;
import com.cloud_kitchen.application.Entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    // Chef rating queries
    List<Rating> findByChefIdOrderByCreatedAtDesc(Long chefId);

    boolean existsByStudentIdAndChefId(Long studentId, Long chefId);

    boolean existsByStudentIdAndChefIdAndOrderId(Long studentId, Long chefId, Long orderId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.chef.id = :chefId")
    Double findAverageRatingByChefId(@Param("chefId") Long chefId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.chef.id = :chefId")
    Long countRatingsByChefId(@Param("chefId") Long chefId);

    // Menu item rating queries
    List<Rating> findByMenuItemIdOrderByCreatedAtDesc(Long menuItemId);

    boolean existsByStudentIdAndMenuItemIdAndOrderId(Long studentId, Long menuItemId, Long orderId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.menuItem.id = :menuItemId")
    Double findAverageRatingByMenuItemId(@Param("menuItemId") Long menuItemId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.menuItem.id = :menuItemId")
    Long countRatingsByMenuItemId(@Param("menuItemId") Long menuItemId);

    // Common queries
    @Query("SELECT r.order.id FROM Rating r WHERE r.student.id = :studentId AND r.chef IS NOT NULL")
    List<Long> findRatedOrderIdsByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT CONCAT(r.order.id, '-', r.menuItem.id) FROM Rating r WHERE r.student.id = :studentId AND r.menuItem IS NOT NULL")
    List<String> findRatedMenuItemsByStudentId(@Param("studentId") Long studentId);

    // Delete by MenuItem
    void deleteByMenuItem(MenuItem menuItem);
}
