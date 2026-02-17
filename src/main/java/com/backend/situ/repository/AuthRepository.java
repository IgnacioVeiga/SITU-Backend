package com.backend.situ.repository;

import com.backend.situ.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<UserCredentials, Long> {
    Optional<UserCredentials> findByEmail(String email);

    Optional<UserCredentials> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
