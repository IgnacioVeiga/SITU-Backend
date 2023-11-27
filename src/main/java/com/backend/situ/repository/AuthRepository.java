package com.backend.situ.repository;

import com.backend.situ.entity.LogInCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<LogInCredentials, Integer> {
    LogInCredentials findByEmail(String email);
}
