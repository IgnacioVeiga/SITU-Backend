package com.backend.situ.controller;

import com.backend.situ.model.ApiResponse;
import com.backend.situ.service.ImageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
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
    public ResponseEntity<ApiResponse<Void>> handleFileUpload(
            @RequestParam("imgFile") MultipartFile imgFile,
            @RequestParam("dni") Integer dni,
            @RequestParam("userId") Long userId
    ) {
        this.imageService.saveUserProfileImage(imgFile, dni, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Imagen de perfil actualizada."));
    }

    @GetMapping("/user-profile/{filename}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        Resource resource = this.imageService.getResource(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/*")
                .body(resource);
    }
}
