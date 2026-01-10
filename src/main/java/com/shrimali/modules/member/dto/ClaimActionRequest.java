package com.shrimali.modules.member.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimActionRequest {
    private ClaimActionType action;
    private String reason;
}
