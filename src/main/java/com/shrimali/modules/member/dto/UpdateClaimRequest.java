package com.shrimali.modules.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateClaimRequest {

    private String requesterFullName;

    private String requesterRelation;

    private String requesterPhotoUrl;
    private String requesterThumbnailUrl;

    private String note;
}
