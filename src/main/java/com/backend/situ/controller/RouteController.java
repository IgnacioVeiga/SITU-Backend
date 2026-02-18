package com.backend.situ.controller;

import com.backend.situ.model.ApiResponse;
import com.backend.situ.model.RouteDTO;
import com.backend.situ.model.RouteUpsertDTO;
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
    public ResponseEntity<ApiResponse<List<RouteDTO>>> getRoutesByLine(
            @PathVariable Long lineId,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        List<RouteDTO> routes = routeService.getRoutesByLine(lineId, subjectEmail);
        return ResponseEntity.ok(ApiResponse.success(routes, null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteDTO>> getRouteById(
            @PathVariable Long id,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        return ResponseEntity.ok(ApiResponse.success(routeService.getRouteById(id, subjectEmail), null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RouteDTO>> createRoute(
            @RequestBody RouteUpsertDTO request,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        return ResponseEntity.ok(ApiResponse.success(routeService.createRoute(request, subjectEmail), "Recorrido creado."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RouteDTO>> updateRoute(
            @PathVariable Long id,
            @RequestBody RouteUpsertDTO request,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        return ResponseEntity.ok(ApiResponse.success(routeService.updateRoute(id, request, subjectEmail), "Recorrido actualizado."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoute(
            @PathVariable Long id,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        routeService.deleteRoute(id, subjectEmail);
        return ResponseEntity.ok(ApiResponse.success(null, "Recorrido eliminado."));
    }
}
