package com.shrimali.modules.shared.services.impl;

import com.shrimali.exceptions.BadRequestException;
import com.shrimali.model.auth.User;
import com.shrimali.model.member.Member;
import com.shrimali.modules.shared.dto.PresignedUrlResponse;
import com.shrimali.modules.shared.services.ImageUploadService;
import com.shrimali.modules.shared.services.SecurityUtils;
import com.shrimali.repositories.MemberRepository;
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
    public PresignedUrlResponse getPresignedUploadUrl(String originalFileName, String contentType) {
        User currentUser = securityUtils.getCurrentUser();
        Member member = memberRepository.findById(currentUser.getMemberId())
                .orElseThrow(() -> new BadRequestException("Member not found"));

        String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));

        String fileName = member.getMembershipNumber() + "_" + System.currentTimeMillis() + extension;

        String objectKey = "profiles/originals/" + fileName;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5)) // 1 min is very short, 5 is safer
                .putObjectRequest(objectRequest)
                .build();

        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();

        return new PresignedUrlResponse(presignedUrl, objectKey);
    }

    @Override
    @Transactional
    public String updateMemberPhoto(String s3Key) {
        User currentUser = securityUtils.getCurrentUser();

        Member member = memberRepository.findById(currentUser.getMemberId())
                .orElseThrow(() -> new BadRequestException("Member not found"));

        // Update the field in your DB entity
        member.setPhotoUrl(s3Key);
        memberRepository.save(member);

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
}
