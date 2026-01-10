package com.shrimali.modules.shared.services;

import com.shrimali.model.enums.MediaOwnerType;
import com.shrimali.modules.shared.dto.PresignedUrlResponse;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

public interface ImageUploadService {
    String uploadProfilePhoto(Principal principal, MultipartFile file);

    PresignedUrlResponse getPresignedUploadUrl(
            MediaOwnerType ownerType, String ownerId, String fileName, String contentType, boolean isThumbnail);

    String updateMemberPhoto(
            MediaOwnerType ownerType, String ownerId, String original, String thumbnailUrl);
}
