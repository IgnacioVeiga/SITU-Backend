package com.backend.situ.entity.image;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "profile_images", schema = "public")
public class ProfileImage extends Image {
    public ProfileImage() {
        super();
    }

    public ProfileImage(String imagePath) {
        super(imagePath);
    }
}
