package com.shrimali.model.member;

import com.shrimali.model.auth.User;
import com.shrimali.modules.member.dto.ClaimActionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "claim_action_logs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClaimActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private MemberClaim claim;

    @Enumerated(EnumType.STRING)
    private ClaimActionType action;

    @ManyToOne(fetch = FetchType.LAZY)
    private User performedBy;

    private String note;

    private LocalDateTime performedAt;
}

