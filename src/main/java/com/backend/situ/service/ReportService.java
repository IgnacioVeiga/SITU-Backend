package com.backend.situ.service;

import com.backend.situ.entity.Report;
import com.backend.situ.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Page<Report> listReports(int pageIndex, int pageSize) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return this.reportRepository.findAll(pageable);
    }

    public Report getReport(Long reportId) {
        return this.reportRepository.findById(reportId).orElse(null);
    }
}
