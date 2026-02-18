package com.backend.situ.repository;

import com.backend.situ.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Page<Complaint> findByCompanyIdOrderByCreatedAtDesc(Long companyId, Pageable pageable);

    Page<Complaint> findByCompanyIdAndReporterUserIdOrderByCreatedAtDesc(Long companyId, Long reporterUserId, Pageable pageable);

    Optional<Complaint> findByIdAndCompanyId(Long id, Long companyId);

    Optional<Complaint> findByTrackingTokenHash(String trackingTokenHash);
}
