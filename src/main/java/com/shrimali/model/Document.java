package com.shrimali.model;

import com.shrimali.model.auth.User;
import com.shrimali.model.enums.DocumentType;
import com.shrimali.model.enums.EntityType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "documents")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Ownership & Metadata ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;

    @Column(nullable = false)
    private String fileUrl;
    private String fileName;
    private String fileType; // e.g., image/jpeg, application/pdf

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    // --- The Generic Linking Logic ---
    @Column(name = "entity_id", nullable = false)
    private Long entityId; // The ID of the Dispute, Member, or Order

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    private EntityType entityType; // DISPUTE, MEMBER_PROFILE, KYC_VERIFICATION

    @CreationTimestamp
    private OffsetDateTime uploadedAt;
}


