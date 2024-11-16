package com.backend.situ.service;

import com.backend.situ.entity.image.ProfileImage;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import com.backend.situ.repository.image.ProfileImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
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
            MultipartFile imgFile,
            Integer dni,
            Long userId
    ){
        try {
            byte[] originalImageBytes = imgFile.getBytes();

            ByteArrayInputStream inputStream = new ByteArrayInputStream(originalImageBytes);
            BufferedImage originalImage = ImageIO.read(inputStream);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BufferedImage imageWithoutMetadata = removeMetadata(originalImage);
            ImageIO.write(imageWithoutMetadata, "jpg", outputStream);

            String newFileName = dni + ".jpg";
            Path uploadPath = Paths.get("files/uploads");
            Files.write(uploadPath.resolve(newFileName), outputStream.toByteArray());

            if (this.profileImageRepository.findById(userId).isEmpty())
            {
                ProfileImage img = new ProfileImage(newFileName);
                this.profileImageRepository.save(img);
            }
            return true;
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private BufferedImage removeMetadata(BufferedImage originalImage) {
        BufferedImage imageWithoutMetadata = new BufferedImage(
                originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        imageWithoutMetadata.createGraphics().drawImage(originalImage, 0, 0, null);
        return imageWithoutMetadata;
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
