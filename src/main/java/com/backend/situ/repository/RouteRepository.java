package com.backend.situ.repository;

import com.backend.situ.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByLineId(Long lineId);
}