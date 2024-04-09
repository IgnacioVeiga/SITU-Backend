package com.backend.situ.repository.image;

import com.backend.situ.entity.image.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    @Override
    Optional<ProfileImage> findById(Long id);
}

