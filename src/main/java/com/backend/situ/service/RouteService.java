package com.backend.situ.service;

import com.backend.situ.entity.Route;
import com.backend.situ.model.RouteDTO;
import com.backend.situ.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RouteService {

    @Autowired
    private RouteRepository routeRepository;

    public List<RouteDTO> getRoutesByLine(Long lineId) {
        List<Object[]> results = routeRepository.findRoutesByLine(lineId);

        List<RouteDTO> routes = new ArrayList<>();
        for (Object[] result : results) {
            Long id = ((Number) result[0]).longValue();
            String name = (String) result[1];
            String coordinates = (String) result[2];

            routes.add(new RouteDTO(id, name, coordinates));
        }

        return routes;
    }

    public Route getRouteById(Long id) {
        return routeRepository.findById(id).orElse(null);
    }

    public Route createRoute(Route route) {
        return routeRepository.save(route);
    }

    public Route updateRoute(Long id, Route routeDetails) {
        Optional<Route> routeOptional = routeRepository.findById(id);
        if (routeOptional.isPresent()) {
            Route route = routeOptional.get();
            route.setName(routeDetails.getName());
            route.setCoordinates(routeDetails.getCoordinates());
            route.setLine(routeDetails.getLine());
            return routeRepository.save(route);
        }
        return null;
    }

    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
    }
}
