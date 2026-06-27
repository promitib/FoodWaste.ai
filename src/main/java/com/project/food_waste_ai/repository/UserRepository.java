package com.project.food_waste_ai.repository;

import com.project.food_waste_ai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find a user by username - used during login
    Optional<User> findByUsername(String username);

    // Check if a username or email is already taken - used during registration
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
