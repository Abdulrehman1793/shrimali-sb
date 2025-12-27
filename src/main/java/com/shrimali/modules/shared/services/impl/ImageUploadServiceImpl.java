package com.shrimali.modules.shared.services.impl;

import com.shrimali.modules.shared.services.ImageUploadService;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ImageUploadServiceImpl implements ImageUploadService {
    private final S3Client s3Client;

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
