package com.shrimali.model.member;

import com.shrimali.model.auth.User;
import com.shrimali.model.enums.OwnershipTransitionReason;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "member_ownership_history")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MemberOwnershipHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "previous_owner_id")
    private User previousOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "new_owner_id")
    private User newOwner;

    @Enumerated(EnumType.STRING)
    @Column(name = "transition_reason", nullable = false)
    private OwnershipTransitionReason reason;

    @Column(name = "reference_id")
    private Long referenceId; // ID of the ClaimRequest or Dispute that caused this

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private User changedBy; // The Admin or the System user who performed the action

    @Column(columnDefinition = "text")
    private String remarks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
