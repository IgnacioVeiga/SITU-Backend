package com.backend.situ.service;

import com.backend.situ.entity.Route;
import com.backend.situ.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteService {

    @Autowired
    private RouteRepository routeRepository;

    public List<Route> getRoutesByLine(Long lineId) {
        return routeRepository.findByLineId(lineId);
    }

    // TODO: Add more methods
}
