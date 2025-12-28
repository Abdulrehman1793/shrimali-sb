package com.shrimali.modules.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberMatchResponse {
    private Long memberId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fatherName;
    private String gotra;
    private String paternalVillage;
    private String gender;
    private String photoUrl;
    private LocalDate dob;
}
