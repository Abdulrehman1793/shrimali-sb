package com.shrimali.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "venue_locations", indexes = {
        @Index(columnList = "state, city", name = "ix_venue_locations_state_city")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String country;
    private String state;
    private String city;

    @Column(name = "area_locality")
    private String areaLocality;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
