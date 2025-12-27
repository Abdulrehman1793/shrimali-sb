package com.shrimali.model.auditing;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;         // e.g., "USER_REGISTRATION", "ROLE_ADDED"

    private String actor;          // The email of the person performing the action

    private String target;         // The email of the user being affected

    @Column(columnDefinition = "TEXT")
    private String details;        // Additional context or JSON data

    private String ipAddress;

    @CreationTimestamp
    private OffsetDateTime timestamp;
}
