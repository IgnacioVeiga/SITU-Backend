package com.backend.situ.controller;

import com.backend.situ.entity.Stop;
import com.backend.situ.model.StopDTO;
import com.backend.situ.service.StopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/situ/stops")
public class StopController {

    @Autowired
    private StopService stopService;

    @GetMapping
    public List<Stop> getAllStops() {
        return stopService.getAllStops();
    }

    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<StopDTO>> getStopsByRoute(@PathVariable Long routeId) {
        List<StopDTO> stops = stopService.getStopsByRoute(routeId);
        return ResponseEntity.ok(stops);
    }

    @PostMapping
    public Stop createStop(@RequestBody Stop stop) {
        return stopService.createStop(stop);
    }

    @PutMapping("/{id}")
    public Stop updateStop(@PathVariable Long id, @RequestBody Stop stopDetails) {
        return stopService.updateStop(id, stopDetails);
    }

    @DeleteMapping("/{id}")
    public void deleteStop(@PathVariable Long id) {
        stopService.deleteStop(id);
    }
}
