package com.pocopi.api.controllers;

import com.pocopi.api.dto.image.ImageUrl;
import com.pocopi.api.services.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping
    public ResponseEntity<ImageUrl> uploadImage(MultipartFile file, String path) {
        final ImageUrl response = imageService.saveImageToExistsUrl(file, path);
        return ResponseEntity.ok(response);
    }
}
