package com.shrimali.modules.shared.services;

import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

public interface ImageUploadService {
    String uploadProfilePhoto(Principal principal, MultipartFile file);
}
