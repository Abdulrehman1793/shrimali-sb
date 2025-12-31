package com.shrimali.modules.member.dto;

import jakarta.validation.constraints.NotBlank;

public record ContactPayload(
        Long id,
        @NotBlank String type,
        Boolean isPrimary,
        @NotBlank String value
) {}

