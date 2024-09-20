package com.backend.situ.repository;

import com.backend.situ.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {
    @Query(value = "SELECT r.id, r.name, ST_AsGeoJSON(r.coordinates) AS coordinates FROM public.routes r WHERE r.line_id = :lineId", nativeQuery = true)
    List<Object[]> findRoutesByLine(@Param("lineId") Long lineId);
}