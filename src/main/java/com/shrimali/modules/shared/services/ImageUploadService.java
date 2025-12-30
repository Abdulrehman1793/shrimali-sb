package com.shrimali.modules.shared.services;

import com.shrimali.modules.shared.dto.PresignedUrlResponse;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

public interface ImageUploadService {
    String uploadProfilePhoto(Principal principal, MultipartFile file);

    PresignedUrlResponse getPresignedUploadUrl(String fileName, String contentType);

    String updateMemberPhoto(String s3Key);
}
