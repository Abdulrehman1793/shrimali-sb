package com.shrimali.model;

import com.shrimali.model.auth.User;
import lombok.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "news_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String slug;
    private String category;

    @Column(name = "content_html", columnDefinition = "text", nullable = false)
    private String contentHtml;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @Builder.Default
    @Column(name = "is_pinned")
    private Boolean isPinned = Boolean.FALSE;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = Boolean.TRUE;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
