package com.shrimali.modules.auth.service;

import com.shrimali.model.auth.Role;
import com.shrimali.model.auth.User;
import com.shrimali.model.auth.UserRole;
import com.shrimali.model.auth.UserSocialAccount;
import com.shrimali.model.enums.AuthProviderType;
import com.shrimali.model.enums.RoleName;
import com.shrimali.model.enums.UserStatus;
import com.shrimali.modules.auth.dto.RegistrationDto;
import com.shrimali.modules.shared.services.AuditService;
import com.shrimali.modules.shared.services.EmailService;
import com.shrimali.repositories.RoleRepository;
import com.shrimali.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuditService auditService;
    private final EmailService emailService;

    @Transactional
    public User registerOrUpdateUser(RegistrationDto dto) {
        // 1. Find or Create the User Account
        // Note: We no longer create a Member inside createNewUser
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseGet(() -> createNewUser(dto));

        // 2. Handle Social Auth Linking
        if (dto.getAuthProvider() != AuthProviderType.LOCAL) {
            handleSocialLink(user, dto);
        }

        // 3. Update metadata
        user.setLastLoginAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        return userRepository.save(user);
    }

    private User createNewUser(RegistrationDto dto) {
        Role guestRole = roleRepository.findByName(RoleName.ROLE_GUEST)
                .orElseThrow(() -> new RuntimeException("Default Role not found"));

        // Build User WITHOUT a Member link
        User user = User.builder()
                .email(dto.getEmail())
                .emailVerified(dto.isEmailVerified())
                .passwordHash(dto.getPassword() != null ? passwordEncoder.encode(dto.getPassword()) : null)
                // Use a status that tells the frontend to show the "Search/Claim" popup
                .status(UserStatus.PENDING_PROFILE)
                .createdAt(OffsetDateTime.now())
                .userRoles(new HashSet<>())
                .socialAccounts(new HashSet<>())
                .build();

        // Bidirectional link for the role
        UserRole userRole = UserRole.builder()
                .role(guestRole)
                .build();
        user.getUserRoles().add(userRole);

        log.info("Creating user account for {} (No member profile linked yet)", dto.getEmail());

        emailService.sendWelcomeEmail(user.getEmail());
        auditService.logAction("USER_ACCOUNT_CREATED", "Account created via " + dto.getAuthProvider());

        return user;
    }

    private void handleSocialLink(User user, RegistrationDto dto) {
        boolean linkExists = user.getSocialAccounts().stream()
                .anyMatch(sa -> sa.getProvider() == dto.getAuthProvider());

        if (!linkExists) {
            UserSocialAccount account = UserSocialAccount.builder()
                    .user(user)
                    .provider(dto.getAuthProvider())
                    .providerId(dto.getProviderId())
                    .linkedAt(OffsetDateTime.now())
                    .build();
            user.getSocialAccounts().add(account);

            auditService.logAction("ACCOUNT_LINKED", "Linked " + dto.getAuthProvider() + " account");
        }
    }
}
