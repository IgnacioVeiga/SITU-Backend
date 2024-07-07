package com.backend.situ.repository;

import com.backend.situ.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthRepository extends JpaRepository<UserCredentials, Long> {
    UserCredentials findByEmail(String email);

    UserCredentials findByUserId(Long userId);
}
