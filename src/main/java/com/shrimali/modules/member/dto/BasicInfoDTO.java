package com.shrimali.modules.member.dto;

import com.shrimali.model.enums.Gender;
import com.shrimali.model.enums.MaritalStatus;
import com.shrimali.model.enums.RoleName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicInfoDTO {
    private Long memberId;

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "First name must contain only English letters")
    private String firstName;

    @NotBlank(message = "Middle name is required")
    @Size(max = 100, message = "Middle name cannot exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Middle name must contain only English letters")
    private String middleName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Last name must contain only English letters")
    private String lastName;

    private Gender gender;
    private LocalDate dob;
    private String email;

    private RoleName role;

    private String photoUrl;
    private String thumbnailUrl;

    private Long gotra;

    private String bloodGroup;
    private MaritalStatus maritalStatus;
    private String profession;
    private Set<String> secondaryProfession;
    private String education;
    private String notes;
    private String kuldevi;
    private String membershipNumber;

    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Paternal village must contain only English letters")
    private String paternalVillage;
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Naniyal village must contain only English letters")
    private String naniyalVillage;

    private Set<String> spokenLanguages;

    private Boolean owner;
}
