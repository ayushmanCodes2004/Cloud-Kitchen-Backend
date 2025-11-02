package com.cloud_kitchen.application.Repository;

import com.cloud_kitchen.application.Entity.Chef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChefRepository extends JpaRepository<Chef, Long> {
    Optional<Chef> findByEmail(String email);
    List<Chef> findByVerified(Boolean verified);
    List<Chef> findBySpecialization(String specialization);
    List<Chef> findByRatingGreaterThanEqual(Double rating);
}
