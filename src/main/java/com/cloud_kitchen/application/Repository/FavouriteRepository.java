package com.cloud_kitchen.application.Repository;

import com.cloud_kitchen.application.Entity.Favourite;
import com.cloud_kitchen.application.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavouriteRepository extends JpaRepository<Favourite, Long> {
    
    List<Favourite> findByStudentOrderByCreatedAtDesc(Student student);
    
    Optional<Favourite> findByStudentAndMenuItemId(Student student, Long menuItemId);
    
    boolean existsByStudentAndMenuItemId(Student student, Long menuItemId);
    
    void deleteByStudentAndMenuItemId(Student student, Long menuItemId);
    
    @Query("SELECT f.menuItem.id FROM Favourite f WHERE f.student = :student")
    List<Long> findMenuItemIdsByStudent(@Param("student") Student student);
}
