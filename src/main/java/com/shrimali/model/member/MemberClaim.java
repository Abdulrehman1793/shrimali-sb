package com.shrimali.model.member;

import com.shrimali.model.auth.User;
import com.shrimali.model.enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_claims")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberClaim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

     /* ======================
       RELATIONSHIPS
       ====================== */

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member targetMember; // The profile being claimed

    @ManyToOne
    @JoinColumn(name = "requester_user_id")
    private User requester; // The person claiming (e.g., the Father)

    @ManyToOne
    @JoinColumn(name = "current_owner_id")
    private User currentOwner; // The person who created the profile (e.g., the Son)

    /* ======================
       CLAIM STATE
       ====================== */

    @Enumerated(EnumType.STRING)
    private ClaimStatus status = ClaimStatus.PENDING; // "PENDING", "APPROVED", "REJECTED"

    /* ======================
       REQUESTER INFO
       ====================== */

    @Column(name = "requester_full_name", length = 200)
    private String requesterFullName;

    @Column(name = "requester_photo_url")
    private String requesterPhotoUrl;

    @Column(name = "requester_thumbnail_url")
    private String requesterThumbnailUrl;

    @Column(name = "requester_relation", length = 50)
    private String requesterRelation; // optional if not already stored

    @Column(name = "identity_note", length = 500)
    private String identityNote; // optional explanation

     /* ======================
       VERIFICATION TOKEN
       ====================== */

    @Column(name = "verification_token_hash")
    private String verificationTokenHash;

    @Column(name = "token_expiry")
    private LocalDateTime tokenExpiry;

     /* ======================
       REMINDER TRACKING
       ====================== */

    @Column(name = "last_reminder_sent_at")
    private LocalDateTime lastReminderSentAt;

    @Column(name = "reminder_count")
    private Integer reminderCount = 0;

     /* ======================
       AUDIT
       ====================== */

    @CreationTimestamp
    private LocalDateTime createdAt;
}
