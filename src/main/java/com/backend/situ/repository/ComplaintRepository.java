package com.backend.situ.repository;

import com.backend.situ.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Page<Complaint> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Complaint> findByReporterUserIdOrderByCreatedAtDesc(Long reporterUserId, Pageable pageable);

    Optional<Complaint> findByTrackingToken(String trackingToken);
}
