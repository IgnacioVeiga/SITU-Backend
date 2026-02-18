package com.backend.situ.controller;

import com.backend.situ.model.ApiResponse;
import com.backend.situ.model.StopDTO;
import com.backend.situ.model.StopUpsertDTO;
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
    public ResponseEntity<ApiResponse<List<StopDTO>>> getAllStops(
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        return ResponseEntity.ok(ApiResponse.success(stopService.getAllStops(subjectEmail), null));
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<ApiResponse<List<StopDTO>>> getStopsByRoute(
            @PathVariable Long routeId,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        List<StopDTO> stops = stopService.getStopsByRoute(routeId, subjectEmail);
        return ResponseEntity.ok(ApiResponse.success(stops, null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StopDTO>> createStop(@RequestBody StopUpsertDTO request) {
        return ResponseEntity.ok(ApiResponse.success(stopService.createStop(request), "Parada creada."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StopDTO>> updateStop(
            @PathVariable Long id,
            @RequestBody StopUpsertDTO request
    ) {
        return ResponseEntity.ok(ApiResponse.success(stopService.updateStop(id, request), "Parada actualizada."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStop(@PathVariable Long id) {
        stopService.deleteStop(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Parada eliminada."));
    }
}
