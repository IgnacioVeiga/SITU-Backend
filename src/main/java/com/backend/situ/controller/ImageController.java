package com.backend.situ.controller;

import com.backend.situ.model.ApiResponse;
import com.backend.situ.service.ImageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/images")
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/upload/user-profile")
    public ResponseEntity<ApiResponse<Boolean>> handleFileUpload(
            @RequestParam("imgFile") MultipartFile imgFile,
            @RequestParam("dni") Integer dni,
            @RequestParam("userId") Long userId
    ) {
        boolean saved = this.imageService.saveUserProfileImage(imgFile, dni, userId);
        if (saved) {
            return ResponseEntity.ok(ApiResponse.success(true, "Imagen de perfil actualizada."));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("ERRORS.GENERIC"));
    }

    @GetMapping("/user-profile/{filename}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        Resource resource = this.imageService.getResource(filename);
        if (resource == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/*")
                .body(resource);
    }
}
