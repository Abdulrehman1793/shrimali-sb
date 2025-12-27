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

    // --- Father's Details ---
    @Column(name = "father_first_name", length = 100)
    private String fatherFirstName;

    @Column(name = "father_middle_name", length = 100)
    private String fatherMiddleName;

    @Column(name = "father_last_name", length = 100)
    private String fatherLastName;

    @Column(name = "paternal_village", length = 200)
    private String paternalVillage; // Father's ancestral home

    // --- Mother's Details ---
    @Column(name = "mother_first_name", length = 100)
    private String motherFirstName;

    @Column(name = "mother_middle_name", length = 100)
    private String motherMiddleName;

    @Column(name = "mother_last_name", length = 100)
    private String motherLastName;

    @Column(name = "naniyal_village", length = 200)
    private String naniyalVillage; // Mother's ancestral home (Naniyal)

    @Column(name = "kuldevi", length = 250)
    private String kuldevi;

    // --- Spouse Details (Manual) ---
    @Column(name = "spouse_first_name", length = 100)
    private String spouseFirstName;

    @Column(name = "spouse_middle_name", length = 100)
    private String spouseMiddleName;

    @Column(name = "spouse_last_name", length = 100)
    private String spouseLastName;

    @Column(name = "spouse_paternal_village", length = 200)
    private String spousePaternalVillage;

    @Column(name = "spouse_naniyal_village", length = 200)
    private String spouseNaniyalVillage;

    @Column(name = "marriage_date")
    private LocalDate marriageDate;

    private String gender;

    private LocalDate dob;

    @Column(name = "marital_status")
    private String maritalStatus;

    private String profession;

    private String education;

    @Column(name = "membership_type")
    private String membershipType;

    @Enumerated(EnumType.STRING)
    @Column(name = "membership_status")
    private MemberShipStatus membershipStatus;

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

    public static String generateMemberNumber(String first, String middle, String last) {
        // 1. Clean and combine names (e.g., "Rahul Kumar Shrimali")
        String fullName = (first + (middle != null ? middle : "") + last)
                .replaceAll("\\s+", "")
                .toUpperCase();

        // 2. Get Initials (e.g., "RKS")
        String initials = "";
        if (first != null && !first.isEmpty()) initials += first.substring(0, 1).toUpperCase();
        if (middle != null && !middle.isEmpty()) initials += middle.substring(0, 1).toUpperCase();
        if (last != null && !last.isEmpty()) initials += last.substring(0, 1).toUpperCase();

        // 3. Create a unique hash based on Full Name + Current Nano Time
        // Using nanoseconds ensures that even if two "Rahul Kumar Shrimalis" join,
        // their IDs will be different.
        String salt = String.valueOf(System.nanoTime());
        int uniqueHash = Math.abs((fullName + salt).hashCode()) % 10000000;

        // 4. Return format: RKS-1234567 (Initials + Unique Number)
        return initials + "-" + String.format("%07d", uniqueHash);
    }

    @PrePersist
    public void ensureMembershipNumber() {
        if (this.membershipNumber == null || this.membershipNumber.isEmpty()) {
            this.membershipNumber = generateMemberNumber(
                    this.firstName,
                    this.middleName,
                    this.lastName
            );
        }
    }
}
