package com.shrimali.modules.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record DiscoverySearchRequest(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "First name must contain only English letters")
        String firstName,

        @NotBlank(message = "Middle name is required")
        @Size(max = 100, message = "Middle name cannot exceed 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Middle name must contain only English letters")
        String middleName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
        @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Last name must contain only English letters")
        String lastName,

        @NotBlank String dob,
        @NotNull Long gotra,
        String gender,
        String relationType,
        @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Paternal village must contain only English letters")
        String paternalVillage,
        @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Naniyal village must contain only English letters")
        String naniyalVillage,
        Boolean deceased
) {
}
