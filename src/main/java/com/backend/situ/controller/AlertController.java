package com.backend.situ.controller;

import com.backend.situ.model.AlertCreateDTO;
import com.backend.situ.model.AlertResponseDTO;
import com.backend.situ.model.AlertUpdateDTO;
import com.backend.situ.model.ApiResponse;
import com.backend.situ.service.AlertService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {
    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/{pageIndex}/{pageSize}")
    public ResponseEntity<ApiResponse<Page<AlertResponseDTO>>> list(
            @PathVariable("pageIndex") int pageIndex,
            @PathVariable("pageSize") int pageSize,
            @RequestParam(name = "activeOnly", defaultValue = "true") boolean activeOnly,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        return ResponseEntity.ok(ApiResponse.success(this.alertService.listAlerts(pageIndex, pageSize, activeOnly, subjectEmail), null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AlertResponseDTO>> create(
            @RequestBody AlertCreateDTO request,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        AlertResponseDTO alert = this.alertService.createAlert(request, subjectEmail);
        return ResponseEntity.ok(ApiResponse.success(alert, "Alerta creada."));
    }

    @PatchMapping("/{alertId}")
    public ResponseEntity<ApiResponse<AlertResponseDTO>> update(
            @PathVariable("alertId") Long alertId,
            @RequestBody AlertUpdateDTO request,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        AlertResponseDTO alert = this.alertService.updateAlert(alertId, request, subjectEmail);
        return ResponseEntity.ok(ApiResponse.success(alert, "Alerta actualizada."));
    }
}
