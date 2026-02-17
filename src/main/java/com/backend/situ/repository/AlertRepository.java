package com.backend.situ.repository;

import com.backend.situ.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    @Query("""
            SELECT a
            FROM Alert a
            WHERE a.active = true
              AND a.startsAt <= :now
              AND (a.endsAt IS NULL OR a.endsAt >= :now)
            """)
    Page<Alert> findActiveAt(
            Timestamp now,
            Pageable pageable
    );
}
