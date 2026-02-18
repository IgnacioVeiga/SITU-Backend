package com.backend.situ.service;

import com.backend.situ.entity.Stop;
import com.backend.situ.entity.User;
import com.backend.situ.entity.UserCredentials;
import com.backend.situ.exception.BadRequestException;
import com.backend.situ.exception.NotFoundException;
import com.backend.situ.model.StopDTO;
import com.backend.situ.model.StopUpsertDTO;
import com.backend.situ.repository.AuthRepository;
import com.backend.situ.repository.RouteRepository;
import com.backend.situ.repository.StopRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StopService {

    private final StopRepository stopRepository;
    private final AuthRepository authRepository;
    private final RouteRepository routeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GeometryFactory geometryFactory = new GeometryFactory();

    public StopService(
            StopRepository stopRepository,
            AuthRepository authRepository,
            RouteRepository routeRepository
    ) {
        this.stopRepository = stopRepository;
        this.authRepository = authRepository;
        this.routeRepository = routeRepository;
    }

    @Transactional(readOnly = true)
    public List<StopDTO> getAllStops(String subjectEmail) {
        Long companyId = resolveUserFromSubject(subjectEmail).getCompany().getId();
        return stopRepository.findAllByCompany(companyId).stream()
                .map(this::toStopDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StopDTO> getStopsByRoute(Long routeId, String subjectEmail) {
        Long companyId = resolveUserFromSubject(subjectEmail).getCompany().getId();
        if (routeRepository.findByIdAndLineCompanyId(routeId, companyId).isEmpty()) {
            throw new NotFoundException("ERRORS.ROUTE.NOT_FOUND");
        }

        return stopRepository.findStopsByRoute(routeId, companyId).stream()
                .map(this::toStopDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public StopDTO createStop(StopUpsertDTO request) {
        validateRequest(request);
        Stop stop = new Stop();
        stop.setName(request.name().trim());
        stop.setLocation(parseGeoJsonPoint(request.locationGeoJson()));
        return toStopDTO(stopRepository.save(stop));
    }

    @Transactional
    public StopDTO updateStop(Long id, StopUpsertDTO request) {
        validateRequest(request);
        Stop stop = stopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ERRORS.STOP.NOT_FOUND"));
        stop.setName(request.name().trim());
        stop.setLocation(parseGeoJsonPoint(request.locationGeoJson()));
        return toStopDTO(stopRepository.save(stop));
    }

    @Transactional
    public void deleteStop(Long id) {
        Stop stop = stopRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ERRORS.STOP.NOT_FOUND"));
        stopRepository.delete(stop);
    }

    private void validateRequest(StopUpsertDTO request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("ERRORS.STOP.NAME_REQUIRED");
        }
        if (request.locationGeoJson() == null || request.locationGeoJson().isBlank()) {
            throw new BadRequestException("ERRORS.STOP.LOCATION_REQUIRED");
        }
    }

    private StopDTO toStopDTO(Stop stop) {
        String location = null;
        if (stop.getLocation() != null) {
            location = String.format(
                    "{\"type\":\"Point\",\"coordinates\":[%s,%s]}",
                    stop.getLocation().getX(),
                    stop.getLocation().getY()
            );
        }
        return new StopDTO(stop.getId(), stop.getName(), location);
    }

    private StopDTO toStopDTO(Object[] row) {
        Long id = ((Number) row[0]).longValue();
        String name = (String) row[1];
        String location = (String) row[2];
        return new StopDTO(id, name, location);
    }

    private Point parseGeoJsonPoint(String locationGeoJson) {
        try {
            JsonNode root = objectMapper.readTree(locationGeoJson);
            String type = root.path("type").asText();
            JsonNode coordinates = root.path("coordinates");
            if (!"Point".equalsIgnoreCase(type) || !coordinates.isArray() || coordinates.size() < 2) {
                throw new BadRequestException("ERRORS.STOP.LOCATION_INVALID");
            }

            double lon = coordinates.get(0).asDouble();
            double lat = coordinates.get(1).asDouble();
            Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
            point.setSRID(4326);
            return point;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("ERRORS.STOP.LOCATION_INVALID");
        }
    }

    private User resolveUserFromSubject(String subjectEmail) {
        UserCredentials credentials = authRepository.findByEmail(subjectEmail)
                .orElseThrow(() -> new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND"));
        if (credentials.getUser() == null) {
            throw new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND");
        }
        return credentials.getUser();
    }
}
