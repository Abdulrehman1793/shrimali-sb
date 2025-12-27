package com.shrimali.model;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "member_contacts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_member_contact",
                        columnNames = {"member_id", "type", "value"}
                )
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberContact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 20)
    private String type; // mobile / email / whatsapp / social

    @Column(nullable = false, length = 255)
    private String value;

    @Builder.Default
    @Column(name = "is_primary")
    private Boolean isPrimary = Boolean.FALSE;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
