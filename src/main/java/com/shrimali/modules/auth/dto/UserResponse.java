package com.shrimali.modules.auth.dto;

import com.shrimali.model.enums.Gender;
import com.shrimali.model.enums.MembershipStatus;
import com.shrimali.model.enums.RoleName;
import com.shrimali.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String firstName;
    private String middleName;
    private String lastName;

    private String email;
    private Boolean emailVerified;
    private String phone;
    private Boolean phoneVerified;

    private Gender gender;
    private String dob;

    private RoleName role;

    private String photoUrl;
    private String thumbnailUrl;

    private UserStatus status;

    private Long memberId;
    private MembershipStatus membershipStatus;

    private boolean completed;
    private int completionPercentage;

    private boolean claimUnderReview;
    private Long claimedMemberId;
    private String claimRequestedAt;
}
