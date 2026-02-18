package com.backend.situ.repository.image;

import com.backend.situ.entity.image.ReportImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReportImageRepository extends JpaRepository<ReportImage, Long> {
    Optional<ReportImage> findByIdAndCompanyId(Long id, Long companyId);
}
