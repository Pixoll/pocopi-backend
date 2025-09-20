package com.pocopi.api.controllers;

import com.pocopi.api.dto.Image.UploadImageResponse;
import com.pocopi.api.services.interfaces.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
public class ImageController {
    ImageService imageService;

    @Autowired
    public void setImageService(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping
    public ResponseEntity<UploadImageResponse> uploadImage(MultipartFile file, String path) {
        UploadImageResponse response = imageService.saveImageToExistsUrl(file, path);
        return ResponseEntity.ok(response);
    }
}
