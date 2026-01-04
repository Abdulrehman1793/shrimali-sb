package com.shrimali.modules.member.dto;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscoveryResponse {

    private boolean exists;
    private List<DiscoveredMemberSummary> member;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiscoveredMemberSummary {
        private Long id;
        private String firstName;
        private String middleName; // Optional
        private String lastName;
        private String paternalVillage;
        private String gotra;

        @Builder.Default
        private boolean matched = false; // Default value for optional flag
    }
}
