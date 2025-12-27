package com.shrimali.modules.member.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberProfileResponse {
    private BasicInfoDTO basicInfo;
    private FatherDetailsDTO father;
    private MotherDetailsDTO mother;
    private SpouseDetailsDTO spouse;
    private ProfileMetadataDTO metadata;
}
