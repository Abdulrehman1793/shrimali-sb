package com.shrimali.modules.member.controller;

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
@RequestMapping("/api/v1/members/media")
@RequiredArgsConstructor
@Validated
public class MemberMediaController {
    private final ImageUploadService imageUploadService;

    @GetMapping("/photo-upload-url")
    public ResponseEntity<PresignedUrlResponse> getUploadUrl(
            @RequestParam("fileName") String fileName,
            @RequestParam("contentType") String contentType,
            @RequestParam(value = "isThumbnail", defaultValue = "false") boolean isThumbnail
    ) {
        // Now passes the isThumbnail flag to the service logic
        PresignedUrlResponse presignedUrl = imageUploadService.getPresignedUploadUrl(fileName, contentType, isThumbnail);
        return ResponseEntity.ok(presignedUrl);
    }

    @PatchMapping("/update-photo-path")
    public ResponseEntity<ProfilePhotoResponse> updatePhotoPath(@RequestBody UpdatePhotoRequest request) {
        String photoUrl = imageUploadService.updateMemberPhoto(request.getPhotoUrl(), request.getThumbnailUrl());

        return ResponseEntity.ok(new ProfilePhotoResponse(photoUrl));
    }
}
