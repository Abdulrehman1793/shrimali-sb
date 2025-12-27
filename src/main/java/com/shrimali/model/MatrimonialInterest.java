package com.shrimali.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "matrimonial_interests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatrimonialInterest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_profile_id", nullable = false)
    private MatrimonialProfile fromProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_profile_id", nullable = false)
    private MatrimonialProfile toProfile;

    private String status; // sent / accepted / rejected / withdrawn

    @Column(columnDefinition = "text")
    private String message;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
