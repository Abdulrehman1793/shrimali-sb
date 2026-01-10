package com.shrimali.modules.shared.services.impl;

import com.shrimali.dto.AuthenticatedIdentity;
import com.shrimali.exceptions.BadRequestException;
import com.shrimali.model.enums.MediaOwnerType;
import com.shrimali.model.member.Member;
import com.shrimali.model.member.MemberClaim;
import com.shrimali.modules.shared.dto.PresignedUrlResponse;
import com.shrimali.modules.shared.services.ImageUploadService;
import com.shrimali.modules.shared.services.SecurityUtils;
import com.shrimali.repositories.MemberRepository;
import com.shrimali.repositories.member.MemberClaimRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.time.Duration;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ImageUploadServiceImpl implements ImageUploadService {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    private final MemberRepository memberRepository;
    private final MemberClaimRepository memberClaimRepository;

    private final SecurityUtils securityUtils;

    @Value("${aws.s3.bucket-name}")
    private String bucket;

    @Override
    public String uploadProfilePhoto(Principal principal, MultipartFile file) {
        validateImage(file);

        String userId = principal.getName(); // or map to userId from DB
        String basePath = "profiles/" + userId + "/";
        String originalKey = basePath + "original.jpg";
        String thumbKey = basePath + "thumb.jpg";

        try {
            // 1️⃣ Upload original image
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(originalKey)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize()
                    )
            );

            // 2️⃣ Generate thumbnail
            ByteArrayOutputStream thumbOut = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    .size(200, 200)
                    .outputFormat("jpg")
                    .toOutputStream(thumbOut);

            // 3️⃣ Upload thumbnail
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(thumbKey)
                            .contentType("image/jpeg")
                            .build(),
                    RequestBody.fromBytes(thumbOut.toByteArray())
            );

            // 4️⃣ Return thumbnail S3 key (store in DB)
            return thumbKey;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile photo", e);
        }
    }


    @Override
    public PresignedUrlResponse getPresignedUploadUrl(
            MediaOwnerType ownerType, String ownerId, String fileName, String contentType, boolean isThumbnail) {
        validateContentType(contentType);

        // 🔐 Authorization
        validateOwnership(ownerType, ownerId);

        String extension = extractExtension(fileName, isThumbnail);
        long timestamp = System.currentTimeMillis();

        String objectKey = buildObjectKey(
                ownerType,
                ownerId,
                isThumbnail,
                timestamp,
                extension
        );

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest =
                PutObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(5))
                        .putObjectRequest(putObjectRequest)
                        .build();

        String uploadUrl =
                s3Presigner.presignPutObject(presignRequest)
                        .url()
                        .toString();

        return new PresignedUrlResponse(uploadUrl, objectKey);
    }

    @Override
    @Transactional
    public String updateMemberPhoto(
            MediaOwnerType ownerType, String ownerId, String s3Key, String thumbnailUrl) {
        validateOwnership(ownerType, ownerId);

        switch (ownerType) {
            case MEMBER -> {
                Member member = memberRepository
                        .findByMembershipNumber(ownerId)
                        .orElseThrow(() ->
                                new BadRequestException("Member not found"));

                member.setPhotoUrl(s3Key);
                member.setThumbnailUrl(thumbnailUrl);
                memberRepository.save(member);
            }

            case CLAIM -> {
                MemberClaim memberClaim = memberClaimRepository.findById(Long.parseLong(ownerId))
                        .orElseThrow(() -> new BadRequestException("Member not found"));
                memberClaim.setRequesterPhotoUrl(s3Key);
                memberClaim.setRequesterThumbnailUrl(thumbnailUrl);
                memberClaimRepository.save(memberClaim);
            }
        }

        return s3Key;
    }

    private void validateImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
            throw new IllegalArgumentException("Only image files allowed");
        }

        // Max 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Image size exceeds 5MB");
        }
    }

    private void validateOwnership(
            MediaOwnerType ownerType, String ownerId) {
        if (ownerType == MediaOwnerType.MEMBER) {
            AuthenticatedIdentity identity = securityUtils.getCurrentIdentity();
            if (!identity.member().getMembershipNumber().equals(ownerId)) {
                throw new BadRequestException("Unauthorized photo update");
            }
        }
        // CLAIM validation can be added here
    }

    private String buildObjectKey(
            MediaOwnerType ownerType,
            String ownerId,
            boolean isThumbnail,
            long timestamp,
            String extension
    ) {
        String base = switch (ownerType) {
            case MEMBER -> "media/members/" + ownerId;
            case CLAIM -> "media/claims/" + ownerId;
        };

        String folder = isThumbnail ? "thumbnails" : "originals";
        return String.format("%s/%s/%d%s", base, folder, timestamp, extension);
    }

    private String extractExtension(String fileName, boolean isThumbnail) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i);
        }
        return isThumbnail ? ".jpg" : "";
    }

    private void validateContentType(String contentType) {
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image uploads allowed");
        }
    }
}
