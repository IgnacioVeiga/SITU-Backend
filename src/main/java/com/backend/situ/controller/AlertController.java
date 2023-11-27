package com.backend.situ.controller;

import com.backend.situ.service.AlertService;
import com.backend.situ.entity.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/situ/alerts")
public class AlertController {
    private final AlertService alertService;

    @Autowired
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/list/{pageIndex}/{pageSize}")
    public Page<Alert> list(@PathVariable("pageIndex") int pageIndex, @PathVariable("pageSize") int pageSize) {
        return this.alertService.listAlerts(pageIndex, pageSize);
    }

    @PostMapping("/create")
    public Alert create(@RequestBody Alert alert) {
        return this.alertService.createAlert(alert);
    }
}

