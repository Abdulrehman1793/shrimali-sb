package com.shrimali.modules.member.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileMetadataDTO {
    private int completionPercentage;
    private String membershipStatus;
    private String gotra;
}
