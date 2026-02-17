package com.backend.situ.controller;

import com.backend.situ.entity.Stop;
import com.backend.situ.model.ApiResponse;
import com.backend.situ.model.StopDTO;
import com.backend.situ.service.StopService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stops")
public class StopController {

    private final StopService stopService;

    public StopController(StopService stopService) {
        this.stopService = stopService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Stop>>> getAllStops() {
        return ResponseEntity.ok(ApiResponse.success(stopService.getAllStops(), null));
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<ApiResponse<List<StopDTO>>> getStopsByRoute(@PathVariable Long routeId) {
        List<StopDTO> stops = stopService.getStopsByRoute(routeId);
        return ResponseEntity.ok(ApiResponse.success(stops, null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Stop>> createStop(@RequestBody Stop stop) {
        return ResponseEntity.ok(ApiResponse.success(stopService.createStop(stop), "Parada creada."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Stop>> updateStop(@PathVariable Long id, @RequestBody Stop stopDetails) {
        return ResponseEntity.ok(ApiResponse.success(stopService.updateStop(id, stopDetails), "Parada actualizada."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStop(@PathVariable Long id) {
        stopService.deleteStop(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Parada eliminada."));
    }
}
