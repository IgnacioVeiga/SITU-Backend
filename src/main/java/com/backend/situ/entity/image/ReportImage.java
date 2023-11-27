package com.backend.situ.entity.image;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "report_images", schema = "public")
public class ReportImage extends Image {
    public ReportImage() {
        super();
    }

    public ReportImage(String imagePath) {
        super(imagePath);
    }
}
