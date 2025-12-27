package com.shrimali.model.geo;

import com.shrimali.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pincodes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"city_id", "pincode"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Pincode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private City city;

    @Column(length = 6, nullable = false)
    private String pincode;

    @Column(nullable = false)
    private boolean active = true;
}
