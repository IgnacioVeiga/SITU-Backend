package com.backend.situ.service;

import com.backend.situ.entity.Company;
import com.backend.situ.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {
    @Autowired
    final private CompanyRepository companyRepository;

    @Autowired
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company createCompany(Company company) {
        return this.companyRepository.save(company);
    }

    public Company getCompany(Long id) {
        return this.companyRepository.findById(id).orElse(null);
    }

    public Boolean existCompanyName(String name) {
        return this.companyRepository.existsByName(name);
    }
}
