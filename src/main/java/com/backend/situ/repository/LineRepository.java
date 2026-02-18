package com.backend.situ.repository;

import com.backend.situ.entity.Line;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LineRepository extends JpaRepository<Line, Long> {
    List<Line> findByCompanyIdOrderByNumberAsc(Long companyId);

    Optional<Line> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByCompanyIdAndNumberIgnoreCase(Long companyId, String number);
}
