package com.backend.situ.service;

import com.backend.situ.entity.image.ProfileImage;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import com.backend.situ.repository.image.ProfileImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageService {
    @Autowired
    private ProfileImageRepository profileImageRepository;

    @Autowired
    public ImageService(ProfileImageRepository profileImageRepository) {
        this.profileImageRepository = profileImageRepository;
    }

    public Boolean saveUserProfileImage(
            MultipartFile file,
            Integer dni
    ){
        try {
            String fileName = file.getOriginalFilename();
            String newFileName = dni + fileName.substring(fileName.lastIndexOf('.'));

            File dest = new File(
                    Paths.get("files/uploads", newFileName).toUri()
            );
            file.transferTo(dest);

            ProfileImage img = new ProfileImage(newFileName);
            this.profileImageRepository.save(img);
            return true;
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public Resource getResource(String filename){
        Path imagePath = Paths.get("files/uploads").resolve(filename);
        try {
            return new UrlResource(imagePath.toUri());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
