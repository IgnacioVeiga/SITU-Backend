package com.backend.situ.service;

import com.backend.situ.entity.Line;
import com.backend.situ.entity.Route;
import com.backend.situ.entity.User;
import com.backend.situ.entity.UserCredentials;
import com.backend.situ.exception.BadRequestException;
import com.backend.situ.exception.NotFoundException;
import com.backend.situ.model.RouteDTO;
import com.backend.situ.model.RouteUpsertDTO;
import com.backend.situ.repository.AuthRepository;
import com.backend.situ.repository.LineRepository;
import com.backend.situ.repository.RouteRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final LineRepository lineRepository;
    private final AuthRepository authRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RouteService(
            RouteRepository routeRepository,
            LineRepository lineRepository,
            AuthRepository authRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.routeRepository = routeRepository;
        this.lineRepository = lineRepository;
        this.authRepository = authRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<RouteDTO> getRoutesByLine(Long lineId, String subjectEmail) {
        Long companyId = resolveUserFromSubject(subjectEmail).getCompany().getId();
        if (lineRepository.findByIdAndCompanyId(lineId, companyId).isEmpty()) {
            throw new NotFoundException("ERRORS.LINE.NOT_FOUND");
        }
        return routeRepository.findByLineIdAndLineCompanyId(lineId, companyId).stream()
                .map(this::toRouteDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RouteDTO getRouteById(Long id, String subjectEmail) {
        Long companyId = resolveUserFromSubject(subjectEmail).getCompany().getId();
        Route route = routeRepository.findByIdAndLineCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("ERRORS.ROUTE.NOT_FOUND"));
        return toRouteDTO(route);
    }

    @Transactional
    public RouteDTO createRoute(RouteUpsertDTO request, String subjectEmail) {
        if (request == null || request.lineId() == null) {
            throw new BadRequestException("ERRORS.ROUTE.LINE_REQUIRED");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("ERRORS.ROUTE.NAME_REQUIRED");
        }
        if (request.coordinates() == null || request.coordinates().isBlank()) {
            throw new BadRequestException("ERRORS.ROUTE.COORDINATES_REQUIRED");
        }

        Long companyId = resolveUserFromSubject(subjectEmail).getCompany().getId();
        Line line = lineRepository.findByIdAndCompanyId(request.lineId(), companyId)
                .orElseThrow(() -> new NotFoundException("ERRORS.LINE.NOT_FOUND"));

        String normalizedCoordinates = normalizeGeoJsonLineString(request.coordinates());

        Long routeId = jdbcTemplate.queryForObject(
                "INSERT INTO public.routes (line_id, name, coordinates) VALUES (?, ?, ST_SetSRID(ST_GeomFromGeoJSON(?), 4326)) RETURNING id",
                Long.class,
                line.getId(),
                request.name().trim(),
                normalizedCoordinates
        );

        if (routeId == null) {
            throw new BadRequestException("ERRORS.ROUTE.INVALID_REQUEST");
        }

        Route created = routeRepository.findByIdAndLineCompanyId(routeId, companyId)
                .orElseThrow(() -> new NotFoundException("ERRORS.ROUTE.NOT_FOUND"));
        return toRouteDTO(created);
    }

    @Transactional
    public RouteDTO updateRoute(Long id, RouteUpsertDTO request, String subjectEmail) {
        Long companyId = resolveUserFromSubject(subjectEmail).getCompany().getId();
        Route route = routeRepository.findByIdAndLineCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("ERRORS.ROUTE.NOT_FOUND"));

        if (request == null) {
            throw new BadRequestException("ERRORS.ROUTE.INVALID_REQUEST");
        }

        Long targetLineId = route.getLine().getId();
        if (request.lineId() != null) {
            Line line = lineRepository.findByIdAndCompanyId(request.lineId(), companyId)
                    .orElseThrow(() -> new NotFoundException("ERRORS.LINE.NOT_FOUND"));
            targetLineId = line.getId();
        }
        String targetName = (request.name() != null && !request.name().isBlank())
                ? request.name().trim()
                : route.getName();

        if (request.coordinates() != null && !request.coordinates().isBlank()) {
            String normalizedCoordinates = normalizeGeoJsonLineString(request.coordinates());
            jdbcTemplate.update(
                    "UPDATE public.routes SET line_id = ?, name = ?, coordinates = ST_SetSRID(ST_GeomFromGeoJSON(?), 4326) WHERE id = ?",
                    targetLineId,
                    targetName,
                    normalizedCoordinates,
                    route.getId()
            );
        } else {
            jdbcTemplate.update(
                    "UPDATE public.routes SET line_id = ?, name = ? WHERE id = ?",
                    targetLineId,
                    targetName,
                    route.getId()
            );
        }

        Route updated = routeRepository.findByIdAndLineCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("ERRORS.ROUTE.NOT_FOUND"));
        return toRouteDTO(updated);
    }

    @Transactional
    public void deleteRoute(Long id, String subjectEmail) {
        Long companyId = resolveUserFromSubject(subjectEmail).getCompany().getId();
        Route route = routeRepository.findByIdAndLineCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("ERRORS.ROUTE.NOT_FOUND"));
        routeRepository.delete(route);
    }

    private User resolveUserFromSubject(String subjectEmail) {
        UserCredentials credentials = authRepository.findByEmail(subjectEmail)
                .orElseThrow(() -> new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND"));
        if (credentials.getUser() == null) {
            throw new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND");
        }
        return credentials.getUser();
    }

    private RouteDTO toRouteDTO(Route route) {
        return new RouteDTO(
                route.getId(),
                route.getName(),
                route.getCoordinates()
        );
    }

    private String normalizeGeoJsonLineString(String coordinatesGeoJson) {
        try {
            JsonNode root = objectMapper.readTree(coordinatesGeoJson);
            String type = root.path("type").asText();
            JsonNode coordinates = root.path("coordinates");

            if (!"LineString".equalsIgnoreCase(type) || !coordinates.isArray() || coordinates.size() < 2) {
                throw new BadRequestException("ERRORS.ROUTE.COORDINATES_INVALID");
            }

            for (JsonNode point : coordinates) {
                if (!point.isArray() || point.size() < 2) {
                    throw new BadRequestException("ERRORS.ROUTE.COORDINATES_INVALID");
                }

                point.get(0).asDouble();
                point.get(1).asDouble();
            }

            return objectMapper.writeValueAsString(root);
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("ERRORS.ROUTE.COORDINATES_INVALID");
        }
    }
}
