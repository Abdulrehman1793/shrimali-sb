package com.shrimali.modules.member.controller;

import com.shrimali.model.enums.MediaOwnerType;
import com.shrimali.modules.member.dto.ProfilePhotoResponse;
import com.shrimali.modules.member.dto.UpdatePhotoRequest;
import com.shrimali.modules.shared.dto.PresignedUrlResponse;
import com.shrimali.modules.shared.services.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Member Media & Assets Controller
 * * * Specialized controller for handling profile photos, thumbnails, and
 * external storage (AWS S3/Cloudfront) integrations.
 * * Responsibilities include generating pre-signed upload URLs and
 * synchronizing file paths with the member record.
 * * Uses {@link com.shrimali.modules.shared.services.ImageUploadService}
 * for storage-specific logic.
 */
@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
@Validated
public class MediaController {
    private final ImageUploadService imageUploadService;

    @GetMapping("/{ownerType}/{ownerId}/photo-upload-url")
    public ResponseEntity<PresignedUrlResponse> getUploadUrl(
            @PathVariable MediaOwnerType ownerType,
            @PathVariable String ownerId,
            @RequestParam("fileName") String fileName,
            @RequestParam("contentType") String contentType,
            @RequestParam(value = "isThumbnail", defaultValue = "false") boolean isThumbnail) {
        // Now passes the isThumbnail flag to the service logic
        PresignedUrlResponse presignedUrl = imageUploadService.getPresignedUploadUrl(ownerType, ownerId, fileName, contentType, isThumbnail);
        return ResponseEntity.ok(presignedUrl);
    }

    @PatchMapping("/{ownerType}/{ownerId}/update-photo-path")
    public ResponseEntity<ProfilePhotoResponse> updatePhotoPath(
            @PathVariable MediaOwnerType ownerType, @PathVariable String ownerId, @RequestBody UpdatePhotoRequest request) {
        String photoUrl = imageUploadService.updateMemberPhoto(ownerType, ownerId, request.getPhotoUrl(), request.getThumbnailUrl());

        return ResponseEntity.ok(new ProfilePhotoResponse(photoUrl));
    }
}
