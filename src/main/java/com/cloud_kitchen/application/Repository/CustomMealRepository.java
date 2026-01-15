package com.cloud_kitchen.application.Repository;

import com.cloud_kitchen.application.Entity.CustomMeal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomMealRepository extends JpaRepository<CustomMeal, Long> {
    
    List<CustomMeal> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    
    List<CustomMeal> findByStudentIdAndAiGeneratedTrueOrderByCreatedAtDesc(Long studentId);
}
