package com.cloud_kitchen.application.Repository;

import com.cloud_kitchen.application.Entity.Chef;
import com.cloud_kitchen.application.Entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByChef(Chef chef);
    List<MenuItem> findByCategory(String category);
    List<MenuItem> findByAvailable(Boolean available);
    List<MenuItem> findByVegetarian(Boolean vegetarian);
    List<MenuItem> findByNameContainingIgnoreCase(String name);
    List<MenuItem> findByPriceBetween(Double minPrice, Double maxPrice);
}
