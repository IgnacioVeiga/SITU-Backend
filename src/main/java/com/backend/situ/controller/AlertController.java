package com.backend.situ.controller;

import com.backend.situ.model.AlertCreateDTO;
import com.backend.situ.model.AlertUpdateDTO;
import com.backend.situ.model.ApiResponse;
import com.backend.situ.service.AlertService;
import com.backend.situ.entity.Alert;
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
    public ResponseEntity<ApiResponse<Page<Alert>>> list(
            @PathVariable("pageIndex") int pageIndex,
            @PathVariable("pageSize") int pageSize
    ) {
        return ResponseEntity.ok(ApiResponse.success(this.alertService.listAlerts(pageIndex, pageSize), null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Alert>> create(
            @RequestBody AlertCreateDTO request,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        Alert alert = this.alertService.createAlert(request, subjectEmail);
        return ResponseEntity.ok(ApiResponse.success(alert, "Alerta creada."));
    }

    @PatchMapping("/{alertId}")
    public ResponseEntity<ApiResponse<Alert>> update(
            @PathVariable("alertId") Long alertId,
            @RequestBody AlertUpdateDTO request,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        Alert alert = this.alertService.updateAlert(alertId, request, subjectEmail);
        return ResponseEntity.ok(ApiResponse.success(alert, "Alerta actualizada."));
    }
}
