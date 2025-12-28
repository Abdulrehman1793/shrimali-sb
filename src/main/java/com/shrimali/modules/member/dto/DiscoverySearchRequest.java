package com.shrimali.modules.member.dto;

import jakarta.validation.constraints.NotBlank;

public record DiscoverySearchRequest(
        @NotBlank String firstName,
        String middleName,
        @NotBlank String lastName,
        @NotBlank String dob,
        String gotra,
        @NotBlank String gender,
        @NotBlank String relationType,
        String paternalVillage,
        String naniyalVillage,
        Boolean deceased
) {}
