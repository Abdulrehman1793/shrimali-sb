package com.shrimali.modules.member.dto;

import com.shrimali.model.enums.Gender;
import com.shrimali.model.enums.MaritalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    private String firstName;
    private String middleName;
    private String lastName;
    private Gender gender;
    private LocalDate dob;
    private String email;
    private String phone;
    private String notes;
    private MaritalStatus maritalStatus;
    private String profession;
    private String education;
}
