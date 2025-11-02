package com.cloud_kitchen.application.Repository;

import com.cloud_kitchen.application.Entity.Role;
import com.cloud_kitchen.application.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    Boolean existsByPhoneNumber(String phoneNumber);
    List<User> findByRole(Role role);
    List<User> findByActive(Boolean active);
}
