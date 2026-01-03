package com.shrimali.modules.auth.service;

import com.shrimali.config.AppConfig;
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
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuditService auditService;
    private final EmailService emailService;

    private final AppConfig appConfig;

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
        // 1. Determine the Role (Prioritize Super Admin > Admin > Guest)
        RoleName targetRoleName;
        if (appConfig.getSuperAdmins().contains(dto.getEmail())) {
            targetRoleName = RoleName.ROLE_SUPER_ADMIN;
        } else if (appConfig.getAdmins().contains(dto.getEmail())) {
            targetRoleName = RoleName.ROLE_ADMIN;
        } else {
            targetRoleName = RoleName.ROLE_GUEST;
        }

        Role role = roleRepository.findByName(targetRoleName)
                .orElseThrow(() -> new RuntimeException("Role " + targetRoleName + " not found in database"));

        // 2. Determine initial UserStatus
        // If email is not verified, they must verify.
        // If it IS verified, they are ACTIVE but still need to link a Member profile.
        UserStatus initialStatus = dto.isEmailVerified() ? UserStatus.ACTIVE : UserStatus.PENDING_VERIFICATION;

        // 3. Build the User
        User user = User.builder()
                .email(dto.getEmail())
                .emailVerified(dto.isEmailVerified())
                .passwordHash(dto.getPassword() != null ? passwordEncoder.encode(dto.getPassword()) : null)
                .status(initialStatus)
                .userRoles(new HashSet<>()) // Initialize to avoid NPE
                .socialAccounts(new HashSet<>())
                .build();

        // 4. Correct Bidirectional Relationship for UserRole
        // Crucial: The UserRole entity needs the 'user' reference if your @JoinColumn is not updatable
        UserRole userRole = UserRole.builder()
                .role(role)
                .build();
        user.getUserRoles().add(userRole);

        log.info("Creating user account for {} with role {} and status {}",
                dto.getEmail(), targetRoleName, initialStatus);

        // 5. Post-creation Actions
        emailService.sendWelcomeEmail(user.getEmail());
        auditService.logAction("USER_ACCOUNT_CREATED", "Account created. Status: " + initialStatus);

        return userRepository.save(user);
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
