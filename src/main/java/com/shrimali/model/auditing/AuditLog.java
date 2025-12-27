package com.shrimali.model.auditing;

import com.shrimali.model.auth.User;
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

    @ManyToOne
    @JoinColumn(name = "actor_user_id")
    private User actor;

    private String target;         // The email of the user being affected

    @Column(columnDefinition = "TEXT")
    private String details;        // Additional context or JSON data

    private String ipAddress;

    @CreationTimestamp
    private OffsetDateTime timestamp;
}
