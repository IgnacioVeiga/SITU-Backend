package com.backend.situ.controller;

import com.backend.situ.entity.Route;
import com.backend.situ.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/situ/routes")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @GetMapping("/line/{lineId}")
    public List<Route> getRoutesByLine(@PathVariable Long lineId) {
        return routeService.getRoutesByLine(lineId);
    }

    // TODO: Add more endpoints
}
