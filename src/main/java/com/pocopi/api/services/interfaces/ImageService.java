package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.Image.Image;
import com.pocopi.api.dto.Image.UploadImageResponse;
import com.pocopi.api.models.image.ImageModel;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    UploadImageResponse saveImageToExistsUrl(MultipartFile file, String path);
    Image getImageByPath(String path);
    ImageModel getImageModelByPath(String path);
    Image getImageById(int id);

    void saveImageBytes(byte[] imageBytes, String relativePath);
    UploadImageResponse createAndSaveImageBytes(byte[] imageBytes, String category, String originalFilename, String alt);
    void deleteImage(String relativePath);
}
