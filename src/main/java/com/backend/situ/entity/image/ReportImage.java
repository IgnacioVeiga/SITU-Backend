package com.backend.situ.entity.image;

import com.backend.situ.entity.Company;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "report_images", schema = "public")
public class ReportImage extends Image {
    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    public ReportImage() {
        super();
    }

    public ReportImage(String imagePath) {
        super(imagePath);
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
