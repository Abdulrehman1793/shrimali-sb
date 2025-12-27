package com.shrimali.model.auditing;

import com.shrimali.model.auth.User;
import com.shrimali.model.enums.MemberShipStatus;
import com.shrimali.model.enums.RoleName;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_role_transitions",
        indexes = {
                @Index(name = "idx_transition_target", columnList = "target_user_id"),
                @Index(name = "idx_transition_actor", columnList = "actor_user_id"),
                @Index(name = "idx_transition_created_at", columnList = "created_at")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleTransition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ============================
       WHO WAS AFFECTED
       ============================ */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id", nullable = false)
    private User targetUser;

    /* ============================
       WHO PERFORMED ACTION
       ============================ */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id", nullable = false)
    private User actorUser;

    /* ============================
       ROLE TRANSITION
       ============================ */
    @Enumerated(EnumType.STRING)
    @Column(name = "from_role", length = 50)
    private RoleName fromRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_role", length = 50)
    private RoleName toRole;

    /* ============================
       MEMBERSHIP TRANSITION
       ============================ */
    @Enumerated(EnumType.STRING)
    @Column(name = "from_membership_status", length = 50)
    private MemberShipStatus fromMembershipStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_membership_status", length = 50)
    private MemberShipStatus toMembershipStatus;

    /* ============================
       AUDIT INFO
       ============================ */
    @Column(columnDefinition = "text")
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}

