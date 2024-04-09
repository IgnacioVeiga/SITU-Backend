package com.backend.situ.controller;

import com.backend.situ.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/situ/images")
public class ImageController {
    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/upload/user-profile")
    public ResponseEntity<Boolean> handleFileUpload(
            @RequestParam("imgFile") MultipartFile imgFile,
            @RequestParam("dni") Integer dni,
            @RequestParam("userId") Long userId
    ) {
        boolean saved = this.imageService.saveUserProfileImage(imgFile, dni, userId);
        if (saved)
            return ResponseEntity.ok(true);
        else
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @GetMapping("/user-profile/{filename}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        Resource resource = this.imageService.getResource(filename);
        if (resource == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        else return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/*")
                .body(resource);
    }
}
