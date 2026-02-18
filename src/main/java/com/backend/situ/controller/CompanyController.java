package com.backend.situ.controller;

import com.backend.situ.model.ApiResponse;
import com.backend.situ.model.CompanySummaryDTO;
import com.backend.situ.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {
    private final CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<CompanySummaryDTO>> getCurrentCompany(
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        return ResponseEntity.ok(ApiResponse.success(this.companyService.getCurrentCompany(subjectEmail), null));
    }
}
