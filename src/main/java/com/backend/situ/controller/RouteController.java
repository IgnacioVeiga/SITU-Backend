package com.backend.situ.controller;

import com.backend.situ.entity.Route;
import com.backend.situ.model.ApiResponse;
import com.backend.situ.model.RouteDTO;
import com.backend.situ.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping("/line/{lineId}")
    public ResponseEntity<ApiResponse<List<RouteDTO>>> getRoutesByLine(@PathVariable Long lineId) {
        List<RouteDTO> routes = routeService.getRoutesByLine(lineId);
        return ResponseEntity.ok(ApiResponse.success(routes, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Route>> getRouteById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(routeService.getRouteById(id), null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Route>> createRoute(@RequestBody Route route) {
        return ResponseEntity.ok(ApiResponse.success(routeService.createRoute(route), "Recorrido creado."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Route>> updateRoute(@PathVariable Long id, @RequestBody Route routeDetails) {
        return ResponseEntity.ok(ApiResponse.success(routeService.updateRoute(id, routeDetails), "Recorrido actualizado."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(@PathVariable Long id) {
        routeService.deleteRoute(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Recorrido eliminado."));
    }
}
