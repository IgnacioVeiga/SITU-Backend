package com.backend.situ.controller;

import com.backend.situ.entity.Report;
import com.backend.situ.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/situ/reports")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/{pageIndex}/{pageSize}")
    public Page<Report> list(@PathVariable("pageIndex") int pageIndex,
                             @PathVariable("pageSize") int pageSize) {
        return this.reportService.listReports(pageIndex, pageSize);
    }

    @GetMapping("/{reportId}")
    public Report get(@PathVariable("reportId") Long reportId) {
        return this.reportService.getReport(reportId);
    }
}
