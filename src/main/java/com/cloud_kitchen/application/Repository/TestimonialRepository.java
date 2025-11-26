package com.cloud_kitchen.application.Repository;

import com.cloud_kitchen.application.Entity.Testimonial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TestimonialRepository extends JpaRepository<Testimonial, Long> {
    List<Testimonial> findByApprovedTrue();
    
    List<Testimonial> findByApprovedFalse();
    
    Optional<Testimonial> findByStudentId(Long studentId);
    
    Optional<Testimonial> findByChefId(Long chefId);
}
