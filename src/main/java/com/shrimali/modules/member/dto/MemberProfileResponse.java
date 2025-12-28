package com.shrimali.modules.member.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberProfileResponse {
    private BasicInfoDTO basicInfo;
    private BasicInfoDTO father;
    private BasicInfoDTO mother;
    private BasicInfoDTO spouse;
    private ProfileMetadataDTO metadata;
}
