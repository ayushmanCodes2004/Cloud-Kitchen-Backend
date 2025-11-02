package com.cloud_kitchen.application.Repository;

import com.cloud_kitchen.application.Entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByEmail(String email);
    List<Admin> findByDepartment(String department);
    List<Admin> findBySuperAdmin(Boolean superAdmin);
}
