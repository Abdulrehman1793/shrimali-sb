package com.shrimali.modules.member.dto;

import com.shrimali.model.enums.Gender;
import com.shrimali.model.enums.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasicInfoDTO {
    private String firstName;
    private String middleName;
    private String lastName;
    private Gender gender;
    private LocalDate dob;
    private String email;

    private RoleName role;

    private String photoUrl;

    private String maritalStatus;
    private String profession;
    private String education;
    private String notes;
    private String kuldevi;
    private String membershipNumber;
}
