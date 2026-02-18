package com.backend.situ.controller;

import com.backend.situ.model.ApiResponse;
import com.backend.situ.model.LineResponseDTO;
import com.backend.situ.model.LineUpsertDTO;
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
    public ResponseEntity<ApiResponse<List<LineResponseDTO>>> getAllLines(
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        return ResponseEntity.ok(ApiResponse.success(lineService.getAllLines(subjectEmail), null));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LineResponseDTO>> getLineById(
            @PathVariable Long id,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        return ResponseEntity.ok(ApiResponse.success(lineService.getLineById(id, subjectEmail), null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LineResponseDTO>> createLine(
            @RequestBody LineUpsertDTO request,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        return ResponseEntity.ok(ApiResponse.success(lineService.createLine(request, subjectEmail), "Línea creada."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LineResponseDTO>> updateLine(
            @PathVariable Long id,
            @RequestBody LineUpsertDTO request,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        return ResponseEntity.ok(ApiResponse.success(lineService.updateLine(id, request, subjectEmail), "Línea actualizada."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLine(
            @PathVariable Long id,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        lineService.deleteLine(id, subjectEmail);
        return ResponseEntity.ok(ApiResponse.success(null, "Línea eliminada."));
    }
}
