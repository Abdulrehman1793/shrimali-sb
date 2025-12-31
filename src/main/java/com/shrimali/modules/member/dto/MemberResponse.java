package com.shrimali.modules.member.dto;

import com.shrimali.model.enums.Gender;
import com.shrimali.model.enums.MemberShipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponse {
    private Long id;
    private String membershipNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private Gender gender;
    private String dob; // ISO date string
    private String membershipType;
    private String membershipStatus;
    private String city;
    private String photoUrl;
    private String thumbnailUrl;
    private String notes;
    private String phone;
    private String maritalStatus;
    private String profession;
    private String education;

    private boolean completed;
}
