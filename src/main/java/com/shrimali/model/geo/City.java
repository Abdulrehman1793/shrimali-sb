package com.shrimali.model.geo;

import com.shrimali.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cities",
        uniqueConstraints = @UniqueConstraint(columnNames = {"state_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class City extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "state_id")
    private State state;

    @Column(length = 120, nullable = false)
    private String name;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private boolean active = true;
}
