package com.shrimali.model.geo;

import com.shrimali.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "states",
        uniqueConstraints = @UniqueConstraint(columnNames = {"country_id", "code"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class State extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "country_id")
    private Country country;

    @Column(length = 10, nullable = false)
    private String code;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active = true;
}

