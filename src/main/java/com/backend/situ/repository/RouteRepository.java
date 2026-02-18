package com.backend.situ.repository;

import com.backend.situ.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByLineIdAndLineCompanyId(Long lineId, Long companyId);

    Optional<Route> findByIdAndLineCompanyId(Long id, Long companyId);
}
