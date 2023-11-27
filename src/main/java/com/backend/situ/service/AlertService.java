package com.backend.situ.service;

import com.backend.situ.entity.Alert;
import com.backend.situ.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public Page<Alert> listAlerts(int pageIndex, int pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return this.alertRepository.findAll(pageable);
    }

    public Alert createAlert(Alert alert) {
        return this.alertRepository.save(alert);
    }
}
