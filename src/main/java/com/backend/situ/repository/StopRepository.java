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
            "JOIN public.routes r ON r.id = rs.route_id " +
            "JOIN public.lines l ON l.id = r.line_id " +
            "WHERE rs.route_id = :routeId AND l.company_id = :companyId " +
            "ORDER BY rs.stop_order", nativeQuery = true)
    List<Object[]> findStopsByRoute(@Param("routeId") Long routeId, @Param("companyId") Long companyId);

    @Query(value = "SELECT DISTINCT s.id, s.name, ST_AsGeoJSON(s.location) AS location " +
            "FROM public.stops s " +
            "JOIN public.routes_stops rs ON rs.stop_id = s.id " +
            "JOIN public.routes r ON r.id = rs.route_id " +
            "JOIN public.lines l ON l.id = r.line_id " +
            "WHERE l.company_id = :companyId", nativeQuery = true)
    List<Object[]> findAllByCompany(@Param("companyId") Long companyId);

    @Query(value = "SELECT EXISTS (" +
            "SELECT 1 " +
            "FROM public.stops s " +
            "JOIN public.routes_stops rs ON rs.stop_id = s.id " +
            "JOIN public.routes r ON r.id = rs.route_id " +
            "JOIN public.lines l ON l.id = r.line_id " +
            "WHERE s.id = :stopId AND l.company_id = :companyId)", nativeQuery = true)
    boolean existsByIdAndCompanyId(@Param("stopId") Long stopId, @Param("companyId") Long companyId);
}
