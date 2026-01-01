package com.shrimali.model.member;

import com.shrimali.model.enums.RelationshipType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member_relationships")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberRelationship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "related_member_id")
    private Member relatedMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type")
    private RelationshipType type; // e.g., "GUARDIAN", "GODPARENT", "BUSINESS_PARTNER"
}
