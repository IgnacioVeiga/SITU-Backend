package com.backend.situ.service;

import com.backend.situ.entity.User;
import com.backend.situ.entity.image.ProfileImage;
import com.backend.situ.exception.BadRequestException;
import com.backend.situ.exception.NotFoundException;
import com.backend.situ.repository.UserRepository;
import com.backend.situ.repository.image.ProfileImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageService.class);
    private static final Path PROFILE_UPLOAD_PATH = Paths.get("files/uploads");

    private final ProfileImageRepository profileImageRepository;
    private final UserRepository userRepository;

    public ImageService(ProfileImageRepository profileImageRepository, UserRepository userRepository) {
        this.profileImageRepository = profileImageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void saveUserProfileImage(
            MultipartFile imgFile,
            Integer dni,
            Long userId
    ) {
        if (imgFile == null || imgFile.isEmpty()) {
            throw new BadRequestException("ERRORS.IMAGE.FILE_REQUIRED");
        }
        if (dni == null || userId == null) {
            throw new BadRequestException("ERRORS.IMAGE.INVALID_REQUEST");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("ERRORS.USER.NOT_FOUND"));

        try {
            byte[] originalImageBytes = imgFile.getBytes();
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalImageBytes));
            if (originalImage == null) {
                throw new BadRequestException("ERRORS.IMAGE.INVALID_FORMAT");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BufferedImage imageWithoutMetadata = removeMetadata(originalImage);
            ImageIO.write(imageWithoutMetadata, "jpg", outputStream);

            Files.createDirectories(PROFILE_UPLOAD_PATH);
            String newFileName = dni + ".jpg";
            Files.write(PROFILE_UPLOAD_PATH.resolve(newFileName), outputStream.toByteArray());

            ProfileImage profileImage = user.getProfileImage();
            if (profileImage == null) {
                profileImage = profileImageRepository.save(new ProfileImage(newFileName));
                user.setProfileImage(profileImage);
            } else {
                profileImage.setFilename(newFileName);
                profileImageRepository.save(profileImage);
            }
            userRepository.save(user);
        } catch (BadRequestException ex) {
            throw ex;
        } catch (IOException ex) {
            LOGGER.error("Could not store profile image for user {}", userId, ex);
            throw new IllegalStateException("ERRORS.GENERIC");
        }
    }

    public Resource getResource(String filename) {
        Path imagePath = PROFILE_UPLOAD_PATH.resolve(filename);
        try {
            Resource resource = new UrlResource(imagePath.toUri());
            if (!resource.exists()) {
                throw new NotFoundException("ERRORS.IMAGE.NOT_FOUND");
            }
            return resource;
        } catch (NotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            LOGGER.error("Could not load image {}", filename, ex);
            throw new IllegalStateException("ERRORS.GENERIC");
        }
    }

    private BufferedImage removeMetadata(BufferedImage originalImage) {
        BufferedImage imageWithoutMetadata = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );
        imageWithoutMetadata.createGraphics().drawImage(originalImage, 0, 0, null);
        return imageWithoutMetadata;
    }
}
