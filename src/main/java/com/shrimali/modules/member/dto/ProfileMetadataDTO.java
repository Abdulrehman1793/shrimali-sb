package com.shrimali.modules.member.dto;

import com.shrimali.model.enums.MembershipStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileMetadataDTO {
    private int completionPercentage;
    private MembershipStatus membershipStatus;
    private String gotra;
}
