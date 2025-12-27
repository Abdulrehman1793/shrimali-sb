package com.shrimali.model;

import com.shrimali.model.enums.RelationshipType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_linkages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberLinkage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_member_id", nullable = false)
    private Member relatedMember;

    @Enumerated(EnumType.STRING)
    private RelationshipType relationshipType;

    private String notes; // e.g., "From the 1995 Pushkar gathering"
}
