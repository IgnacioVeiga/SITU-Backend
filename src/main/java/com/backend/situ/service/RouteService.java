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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final LineRepository lineRepository;
    private final AuthRepository authRepository;

    public RouteService(RouteRepository routeRepository, LineRepository lineRepository, AuthRepository authRepository) {
        this.routeRepository = routeRepository;
        this.lineRepository = lineRepository;
        this.authRepository = authRepository;
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

        Route route = new Route();
        route.setLine(line);
        route.setName(request.name().trim());
        route.setCoordinates(request.coordinates().trim());
        return toRouteDTO(routeRepository.save(route));
    }

    @Transactional
    public RouteDTO updateRoute(Long id, RouteUpsertDTO request, String subjectEmail) {
        Long companyId = resolveUserFromSubject(subjectEmail).getCompany().getId();
        Route route = routeRepository.findByIdAndLineCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("ERRORS.ROUTE.NOT_FOUND"));

        if (request == null) {
            throw new BadRequestException("ERRORS.ROUTE.INVALID_REQUEST");
        }

        if (request.lineId() != null) {
            Line line = lineRepository.findByIdAndCompanyId(request.lineId(), companyId)
                    .orElseThrow(() -> new NotFoundException("ERRORS.LINE.NOT_FOUND"));
            route.setLine(line);
        }
        if (request.name() != null && !request.name().isBlank()) {
            route.setName(request.name().trim());
        }
        if (request.coordinates() != null && !request.coordinates().isBlank()) {
            route.setCoordinates(request.coordinates().trim());
        }

        return toRouteDTO(routeRepository.save(route));
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
}
