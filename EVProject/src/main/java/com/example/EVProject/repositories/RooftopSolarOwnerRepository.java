package com.example.EVProject.repositories;

import com.example.EVProject.model.RooftopSolarOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RooftopSolarOwnerRepository extends JpaRepository<RooftopSolarOwner, Integer> {
    Optional<RooftopSolarOwner> findByUsername(String username);
    boolean existsByUsername(String username);


}