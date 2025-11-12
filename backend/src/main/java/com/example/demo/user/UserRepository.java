package com.example.demo.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 *
 * Author : bhask
 * Created : 11-11-2025
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
