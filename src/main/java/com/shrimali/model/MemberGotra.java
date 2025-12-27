package com.shrimali.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "member_gotras", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "gotra_id"}, name = "ux_member_gotra")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberGotra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gotra_id", nullable = false)
    private Gotra gotra;

    @Builder.Default
    @Column(name = "is_primary")
    private Boolean isPrimary = Boolean.TRUE;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
