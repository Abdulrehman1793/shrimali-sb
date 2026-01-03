package com.shrimali.modules.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DiscoverySearchRequest(
        @NotBlank String firstName,
        @NotBlank String middleName,
        @NotBlank String lastName,
        @NotBlank String dob,
        @NotNull Long gotra,
        @NotBlank String gender,
        String relationType,
        String paternalVillage,
        String naniyalVillage,
        Boolean deceased
) {
}
