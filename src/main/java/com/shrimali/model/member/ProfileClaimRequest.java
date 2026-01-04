package com.shrimali.model.member;

import com.shrimali.model.auth.User;
import com.shrimali.model.enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "profile_claim_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileClaimRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member targetMember; // The profile being claimed

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_user_id", nullable = false)
    private User requester; // The person saying "This is me"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_owner_id")
    private User currentOwner; // The person who currently manages the ghost profile

    @Enumerated(EnumType.STRING)
    private ClaimStatus status = ClaimStatus.PENDING; // PENDING, APPROVED, REJECTED, DISPUTED

    @Column(columnDefinition = "text")
    private String requesterNote; // Why I am claiming this

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    private OffsetDateTime resolvedAt;
}


