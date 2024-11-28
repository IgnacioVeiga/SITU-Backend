package com.backend.situ.service;

import com.backend.situ.entity.Company;
import com.backend.situ.enums.AuditAction;
import com.backend.situ.event.AuditEvent;
import com.backend.situ.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {
    @Autowired
    final private CompanyRepository companyRepository;
    
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public CompanyService(ApplicationEventPublisher eventPublisher, CompanyRepository companyRepository) {
        this.eventPublisher = eventPublisher;
        this.companyRepository = companyRepository;
    }

    public Company createCompany(Company company) {
        String details = "New company: " + company.getName();
        // TODO: get admin company username
        AuditEvent auditEvent = new AuditEvent(this, AuditAction.NEW_COMPANY, "", details);
        eventPublisher.publishEvent(auditEvent);
        
        return this.companyRepository.save(company);
    }

    public Company getCompany(Long id) {
        return this.companyRepository.findById(id).orElse(null);
    }

    public Boolean existCompanyName(String name) {
        return this.companyRepository.existsByName(name);
    }
}
