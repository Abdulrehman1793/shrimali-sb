package com.shrimali.model;

import com.shrimali.model.auth.User;
import com.shrimali.model.member.Member;
import lombok.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "matrimonial_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatrimonialProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // optional link to member
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "profile_code", nullable = false, unique = true)
    private String profileCode;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(nullable = false)
    private String gender;

    private LocalDate dob;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "marital_status")
    private String maritalStatus;

    private String education;

    private String occupation;

    @Column(name = "income_range")
    private String incomeRange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gotra_id")
    private Gotra gotra;

    private String city;
    private String state;
    private String country;

    @Column(name = "mother_tongue")
    private String motherTongue;

    private String diet;

    @Column(name = "manglik_status")
    private String manglikStatus;

    @Column(columnDefinition = "text")
    private String about;

    @Column(name = "family_details", columnDefinition = "text")
    private String familyDetails;

    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "profile_status")
    private String profileStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
