package com.shrimali.model.member;

import com.shrimali.model.auth.User;
import com.shrimali.model.enums.DisputeResolution;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "claim_disputes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimDispute {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "claim_request_id", nullable = false)
    private ProfileClaimRequest claimRequest;

    // --- Dispute Status ---
    // Track if both parties have finished uploading their evidence
    @Builder.Default
    private boolean requesterEvidenceSubmitted = false;

    @Builder.Default
    private boolean ownerEvidenceSubmitted = false;

    private String requesterIdDocUrl;
    private String ownerIdDocUrl;

    @Column(columnDefinition = "text")
    private String adminInternalNotes;

    // --- Decision Metadata ---
    @Enumerated(EnumType.STRING)
    @Column(name = "final_decision")
    private DisputeResolution finalDecision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_admin_id")
    private User resolvedBy;

    // --- Timestamps ---
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    private OffsetDateTime resolvedAt;

    /**
     * Logic check to see if the Admin can start reviewing.
     */
    public boolean isReadyForReview() {
        return requesterEvidenceSubmitted && ownerEvidenceSubmitted;
    }
}
