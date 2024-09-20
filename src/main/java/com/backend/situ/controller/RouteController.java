package com.backend.situ.controller;

import com.backend.situ.entity.Route;
import com.backend.situ.model.RouteDTO;
import com.backend.situ.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/situ/routes")
public class RouteController {

    @Autowired
    private RouteService routeService;

    @GetMapping("/line/{lineId}")
    public ResponseEntity<List<RouteDTO>> getRoutesByLine(@PathVariable Long lineId) {
        List<RouteDTO> routes = routeService.getRoutesByLine(lineId);
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/{id}")
    public Route getRouteById(@PathVariable Long id) {
        return routeService.getRouteById(id);
    }

    @PostMapping
    public Route createRoute(@RequestBody Route route) {
        return routeService.createRoute(route);
    }

    @PutMapping("/{id}")
    public Route updateRoute(@PathVariable Long id, @RequestBody Route routeDetails) {
        return routeService.updateRoute(id, routeDetails);
    }

    @DeleteMapping("/{id}")
    public void deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
    }
}
