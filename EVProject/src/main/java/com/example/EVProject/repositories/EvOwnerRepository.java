package com.example.EVProject.repositories;

import com.example.EVProject.model.EvOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EvOwnerRepository extends JpaRepository<EvOwner, Integer> {
    Optional<EvOwner> findByUsername(String username);
    boolean existsByUsername(String username);
}