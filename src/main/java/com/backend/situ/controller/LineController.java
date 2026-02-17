package com.backend.situ.controller;

import com.backend.situ.entity.Line;
import com.backend.situ.model.ApiResponse;
import com.backend.situ.service.LineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lines")
public class LineController {

    private final LineService lineService;

    public LineController(LineService lineService) {
        this.lineService = lineService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Line>>> getAllLines() {
        return ResponseEntity.ok(ApiResponse.success(lineService.getAllLines(), null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Line>> getLineById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(lineService.getLineById(id), null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Line>> createLine(@RequestBody Line line) {
        return ResponseEntity.ok(ApiResponse.success(lineService.createLine(line), "Línea creada."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Line>> updateLine(@PathVariable Long id, @RequestBody Line lineDetails) {
        return ResponseEntity.ok(ApiResponse.success(lineService.updateLine(id, lineDetails), "Línea actualizada."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLine(@PathVariable Long id) {
        lineService.deleteLine(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Línea eliminada."));
    }
}
