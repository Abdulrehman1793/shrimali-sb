package com.shrimali.modules.member.dto;

import jakarta.validation.constraints.NotBlank;

public record ContactPayload(
        @NotBlank String type,
        Boolean isPrimary,
        @NotBlank String value
) {}

