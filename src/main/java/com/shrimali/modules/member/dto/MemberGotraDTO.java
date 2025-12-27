package com.shrimali.modules.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberGotraDTO {
    private Long id;
    private Long gotraId;
    private String name;
    private String description;
    private Boolean isPrimary;
}
