package com.backend.situ.service;

import com.backend.situ.entity.Company;
import com.backend.situ.entity.UserCredentials;
import com.backend.situ.enums.AuditAction;
import com.backend.situ.event.AuditEvent;
import com.backend.situ.exception.BadRequestException;
import com.backend.situ.model.CompanySummaryDTO;
import com.backend.situ.repository.AuthRepository;
import com.backend.situ.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {
    @Autowired
    final private CompanyRepository companyRepository;
    private final AuthRepository authRepository;
    
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public CompanyService(
            ApplicationEventPublisher eventPublisher,
            CompanyRepository companyRepository,
            AuthRepository authRepository
    ) {
        this.eventPublisher = eventPublisher;
        this.companyRepository = companyRepository;
        this.authRepository = authRepository;
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

    public CompanySummaryDTO getCurrentCompany(String subjectEmail) {
        UserCredentials credentials = authRepository.findByEmail(subjectEmail)
                .orElseThrow(() -> new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND"));
        if (credentials.getUser() == null || credentials.getUser().getCompany() == null) {
            throw new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND");
        }

        Company company = credentials.getUser().getCompany();
        return new CompanySummaryDTO(company.getId(), company.getName(), company.getLogo_filename());
    }
}
