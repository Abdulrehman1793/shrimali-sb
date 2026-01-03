package com.shrimali.modules.member.dto;

import java.util.List;
import java.util.UUID;

public record DiscoveryResponse(
        boolean exists,
        List<MemberSummary> member
) {
    public record MemberSummary(
            Long id,
            String firstName,
            String lastName,
            String paternalVillage,
            String gotra
    ) {}
}
