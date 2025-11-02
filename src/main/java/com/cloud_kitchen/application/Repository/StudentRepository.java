package com.cloud_kitchen.application.Repository;

import com.cloud_kitchen.application.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    Optional<Student> findByStudentId(String studentId);
    Boolean existsByStudentId(String studentId);
    List<Student> findByCollege(String college);
    List<Student> findByHostelName(String hostelName);
}
