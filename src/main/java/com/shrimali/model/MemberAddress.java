package com.shrimali.model;

import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "member_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "address_type", nullable = false)
    private String addressType; // current / permanent

    @Column(nullable = false)
    private String line1;

    private String line2;

    @Column(name = "area_locality")
    private String areaLocality;

    @Column(nullable = false)
    private String city;

    private String district;

    @Column(nullable = false)
    private String state;

    private String country;

    private String pincode;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
