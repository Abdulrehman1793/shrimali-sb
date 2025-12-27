package com.shrimali.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "matrimonial_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatrimonialPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matrimonial_profile_id", nullable = false)
    private MatrimonialProfile profile;

    private Integer minAge;
    private Integer maxAge;
    private Integer minHeightCm;
    private Integer maxHeightCm;

    @Column(name = "preferred_cities", columnDefinition = "text")
    private String preferredCities;

    @Column(name = "preferred_states", columnDefinition = "text")
    private String preferredStates;

    @Column(name = "preferred_countries", columnDefinition = "text")
    private String preferredCountries;

    @Column(name = "preferred_gotra_ids", columnDefinition = "text")
    private String preferredGotraIds;

    @Column(name = "preferred_education")
    private String preferredEducation;

    @Column(name = "preferred_occupation")
    private String preferredOccupation;

    @Column(name = "preferred_diet")
    private String preferredDiet;

    @Column(name = "manglik_preference")
    private String manglikPreference;

    @Column(name = "other_preferences", columnDefinition = "text")
    private String otherPreferences;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
