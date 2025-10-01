package com.pocopi.api.services.interfaces;

import com.pocopi.api.dto.Image.SingleImageResponse;
import com.pocopi.api.dto.Image.UploadImageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    UploadImageResponse saveImageToExistsUrl(MultipartFile file, String alt);

    SingleImageResponse getImageByPath(String path);
}
