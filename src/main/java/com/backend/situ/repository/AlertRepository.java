package com.backend.situ.repository;

import com.backend.situ.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    @Query("""
            SELECT a
            FROM Alert a
            WHERE a.company.id = :companyId
              AND a.active = true
              AND a.startsAt <= :now
              AND (a.endsAt IS NULL OR a.endsAt >= :now)
            """)
    Page<Alert> findActiveAt(
            Long companyId,
            Timestamp now,
            Pageable pageable
    );

    Page<Alert> findByCompanyIdOrderByAlertDateDesc(Long companyId, Pageable pageable);

    Optional<Alert> findByIdAndCompanyId(Long id, Long companyId);
}
