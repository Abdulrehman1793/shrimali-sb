package com.shrimali.modules.member.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MemberProfileResponse {
    private BasicInfoDTO basicInfo;
    private BasicInfoDTO father;
    private BasicInfoDTO mother;
    private BasicInfoDTO spouse;
    private List<MemberAddressPayload> addresses;
    private List<ContactPayload> contacts;
    private ProfileMetadataDTO metadata;
}
