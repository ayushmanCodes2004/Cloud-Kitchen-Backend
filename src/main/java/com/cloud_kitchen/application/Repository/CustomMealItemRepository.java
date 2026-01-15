package com.cloud_kitchen.application.Repository;

import com.cloud_kitchen.application.Entity.CustomMealItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomMealItemRepository extends JpaRepository<CustomMealItem, Long> {
    
    List<CustomMealItem> findByCustomMealId(Long customMealId);
}
