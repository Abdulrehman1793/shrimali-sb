package com.shrimali.model;

import com.shrimali.model.auth.User;
import com.shrimali.model.enums.MemberShipStatus;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.Set;

    @Entity
    @Table(name = "members")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Member {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "membership_number", nullable = false, unique = true, length = 50)
        private String membershipNumber;

        @Column(name = "first_name", nullable = false, length = 100)
        private String firstName;

        @Column(name = "middle_name", length = 100)
        private String middleName;

        @Column(name = "last_name", nullable = false, length = 100)
        private String lastName;

        // --- CRITICAL FIX: Direct Object Links ---
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "father_id")
        private Member father; // Points to the Father's Member record

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "mother_id")
        private Member mother; // Points to the Mother's Member record

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "spouse_id")
        private Member spouse; // Points to the Spouse's Member record

        // --- Status Flags ---
        @Column(name = "is_deceased")
        private boolean deceased = false;

        @Column(name = "date_of_death")
        private LocalDate dateOfDeath;

        // --- Ownership (For Ghost Profiles) ---
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "owner_user_id")
        private User owner; // The user who created this profile (e.g., the Son)

        @OneToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "linked_user_id")
        private User linkedUser; // The user who "claimed" this profile

        // --- Ancestral Metadata ---
        @Column(name = "paternal_village", length = 200)
        private String paternalVillage;

        @Column(name = "naniyal_village", length = 200)
        private String naniyalVillage;

        @Column(name = "kuldevi", length = 250)
        private String kuldevi;

        @Column(name = "marriage_date")
        private LocalDate marriageDate;

        // --- Other Fields ---
        private String gender;
        private LocalDate dob;
        private String profession;
        private String education;

        @Column(name = "marital_status")
        private String maritalStatus;

        @Column(name = "membership_type")
        private String membershipType;

        @Column(name = "membership_status")
        private String membershipStatus;

        @CreationTimestamp
        @Column(name = "joined_at")
        private OffsetDateTime joinedAt;

        @Column(name = "approved_at")
        private OffsetDateTime approvedAt;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "approved_by_user_id")
        private User approvedBy;

        @Column(columnDefinition = "text")
        private String notes;

        @Column(name = "photo_url")
        private String photoUrl;

        @CreationTimestamp
        @Column(name = "created_at")
        private OffsetDateTime createdAt;

        @UpdateTimestamp
        @Column(name = "updated_at")
        private OffsetDateTime updatedAt;

        @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<MemberContact> contacts;

        @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<MemberAddress> addresses;

        // --- Logic for Membership Number ---
        @PrePersist
        public void ensureMembershipNumber() {
            if (this.membershipNumber == null) {
                this.membershipNumber = generateMemberNumber(firstName, middleName, lastName);
            }
        }

        public static String generateMemberNumber(String first, String middle, String last) {
            String initials = (first.charAt(0) +
                    (middle != null && !middle.isEmpty() ? middle.substring(0,1) : "") +
                    last.charAt(0)).toUpperCase();
            String salt = String.valueOf(System.nanoTime());
            int uniqueHash = Math.abs((first + last + salt).hashCode()) % 10000000;
            return initials + "-" + String.format("%07d", uniqueHash);
        }
    }
