package com.shrimali.modules.auth.dto;

public record ResetPasswordRequest(
        String tokenId,
        String token,
        String newPassword
) {}

