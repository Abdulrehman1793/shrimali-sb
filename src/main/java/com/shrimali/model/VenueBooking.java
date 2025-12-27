package com.shrimali.model;

import com.shrimali.model.auth.User;
import lombok.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "venue_bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "start_datetime", nullable = false)
    private OffsetDateTime startDatetime;

    @Column(name = "end_datetime", nullable = false)
    private OffsetDateTime endDatetime;

    private String status;

    @Column(name = "expected_guests")
    private Integer expectedGuests;

    @Column(name = "total_amount")
    private java.math.BigDecimal totalAmount;

    @Column(name = "advance_amount")
    private java.math.BigDecimal advanceAmount;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
