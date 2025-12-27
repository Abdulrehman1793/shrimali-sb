package com.shrimali.model.auth;

import com.shrimali.model.enums.AuthProviderType;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_social_accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSocialAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProviderType provider; // GOOGLE, GITHUB, etc.

    @Column(name = "provider_id", nullable = false)
    private String providerId; // The "sub" or "id" from the OAuth provider

    private OffsetDateTime linkedAt;
}
