package com.shrimali.model;

import com.shrimali.model.auth.User;
import com.shrimali.model.enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_claims")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberClaim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member targetMember; // The profile being claimed

    @ManyToOne
    @JoinColumn(name = "requester_user_id")
    private User requester; // The person claiming (e.g., the Father)

    @ManyToOne
    @JoinColumn(name = "current_owner_id")
    private User currentOwner; // The person who created the profile (e.g., the Son)

    @Enumerated(EnumType.STRING)
    private ClaimStatus status = ClaimStatus.PENDING; // "PENDING", "APPROVED", "REJECTED"

    // Token Logic for Authentication
    private String verificationTokenHash;
    private LocalDateTime tokenExpiry;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
