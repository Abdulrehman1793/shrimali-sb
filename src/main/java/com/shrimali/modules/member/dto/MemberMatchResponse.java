package com.shrimali.modules.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberMatchResponse {
    private Long memberId;
    private String fullName;
    private String fatherName; // Derived from father member object
    private String paternalVillage;
    private String gender;
    private String photoUrl;
}
