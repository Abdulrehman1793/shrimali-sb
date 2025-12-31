package com.shrimali.modules.member.controller;

import com.shrimali.modules.shared.services.ImageUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Validated
public class MemberMediaController {
    private final ImageUploadService imageUploadService;
}
