package com.shrimali.model.donation;

import com.shrimali.model.auth.User;
import com.shrimali.model.member.Member;
import lombok.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "donations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    private DonationCampaign campaign;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "donor_name", nullable = false)
    private String donorName;

    @Column(name = "donor_email")
    private String donorEmail;

    @Column(name = "donor_phone")
    private String donorPhone;

    private java.math.BigDecimal amount;
    private String currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_transaction_id")
    private PaymentTransaction paymentTransaction;

    @Column(name = "receipt_number")
    private String receiptNumber;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "donated_at")
    private OffsetDateTime donatedAt;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
