package com.shrimali.modules.member.dto;

import com.shrimali.model.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberPayload {
    private String firstName;
    private String middleName;
    private String lastName;
    private Gender gender;
    private String dob;
    private String email;
    private String phone;
}
