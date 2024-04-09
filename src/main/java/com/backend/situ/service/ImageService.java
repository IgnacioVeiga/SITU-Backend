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
            // Leer la imagen original en un arreglo de bytes
            byte[] originalImageBytes = imgFile.getBytes();

            // Convertir la imagen original a BufferedImage
            ByteArrayInputStream inputStream = new ByteArrayInputStream(originalImageBytes);
            BufferedImage originalImage = ImageIO.read(inputStream);

            // Crear un ByteArrayOutputStream para escribir la imagen convertida
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Eliminar los metadatos de la imagen
            BufferedImage imageWithoutMetadata = removeMetadata(originalImage);

            // Escribir la imagen sin metadatos como JPG en el ByteArrayOutputStream
            ImageIO.write(imageWithoutMetadata, "jpg", outputStream);

            // Guardar la imagen sin metadatos como JPG
            String newFileName = dni + ".jpg"; // Nombre del archivo basado en el DNI
            Path uploadPath = Paths.get("files/uploads"); // Ruta donde guardar la imagen
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
        // Crear una nueva imagen sin metadatos
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
