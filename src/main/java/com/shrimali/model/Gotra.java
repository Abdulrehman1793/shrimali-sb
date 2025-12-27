package com.shrimali.model;

import com.shrimali.model.auth.User;
import lombok.*;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "gotras",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_gotra_name", columnNames = "name")
        },
        indexes = {
                @Index(name = "idx_gotra_name", columnList = "name"),
                @Index(name = "idx_gotra_core", columnList = "is_core")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gotra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, unique = true, nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    /**
     * Indicates whether this is a core Saptarishi gotra
     */
    @Column(name = "is_core", nullable = false)
    private boolean core;

    /**
     * Parent gotra (for sub-gotras / lineage mapping)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_gotra_id")
    private Gotra parentGotra;

    /**
     * Audit fields
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
