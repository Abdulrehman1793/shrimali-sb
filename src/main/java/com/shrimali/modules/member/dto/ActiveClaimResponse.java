package com.shrimali.modules.member.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Builder
public class ActiveClaimResponse {

    private Long claimId;

    // ─────────────────────────────
    // Target member (READ-ONLY)
    // ─────────────────────────────
    private TargetMember targetMember;

    // ─────────────────────────────
    // Requester temporary details
    // ─────────────────────────────
    private String requesterFullName;
    private String requesterRelation;
    private String requesterPhotoUrl;
    private String requesterThumbnailUrl;
    private String note;

    private String status;
    private LocalDateTime lastReminderSentAt;
    private Integer reminderCount;

    // ─────────────────────────────
    // Nested DTO
    // ─────────────────────────────
    @Data
    @Builder
    public static class TargetMember {
        private Long id;
        private String fullName;
        private String photoUrl;
        private String relation;
    }
}
