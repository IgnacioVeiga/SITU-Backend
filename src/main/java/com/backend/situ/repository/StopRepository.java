package com.backend.situ.repository;

import com.backend.situ.entity.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StopRepository extends JpaRepository<Stop, Long> {
    @Query(value = "SELECT s.id, s.name, ST_AsGeoJSON(s.location) AS location " +
            "FROM public.stops s " +
            "JOIN public.routes_stops rs ON rs.stop_id = s.id " +
            "WHERE rs.route_id = :routeId", nativeQuery = true)
    List<Object[]> findStopsByRoute(@Param("routeId") Long routeId);
}