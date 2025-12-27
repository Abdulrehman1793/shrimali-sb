package com.shrimali.model.donation;

import lombok.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "donation_campaigns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String code;
    @Column(columnDefinition = "text")
    private String description;
    @Column(name = "target_amount")
    private java.math.BigDecimal targetAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = Boolean.TRUE;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
