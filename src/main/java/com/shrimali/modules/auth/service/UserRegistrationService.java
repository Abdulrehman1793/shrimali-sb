package com.shrimali.modules.auth.service;

import com.shrimali.model.Member;
import com.shrimali.model.auth.Role;
import com.shrimali.model.auth.User;
import com.shrimali.model.auth.UserRole;
import com.shrimali.model.auth.UserSocialAccount;
import com.shrimali.model.enums.AuthProviderType;
import com.shrimali.model.enums.RoleName;
import com.shrimali.modules.auth.dto.RegistrationDto;
import com.shrimali.modules.shared.services.AuditService;
import com.shrimali.modules.shared.services.EmailService;
import com.shrimali.repositories.MemberRepository;
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
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final AuditService auditService;
    private final EmailService emailService;

    @Transactional
    public User registerOrUpdateUser(RegistrationDto dto) {
        // 1. Find or Create User
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseGet(() -> createNewUserAndMember(dto));

        // 2. If it's a Social Login, handle the Link
        if (dto.getAuthProvider() != AuthProviderType.LOCAL) {
            handleSocialLink(user, dto);
        }

        // 3. Update login metadata
        user.setLastLoginAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        return userRepository.save(user);
    }

    private User createNewUserAndMember(RegistrationDto dto) {
        Role guestRole = roleRepository.findByName(RoleName.ROLE_GUEST)
                .orElseThrow(() -> new RuntimeException("Default Role not found"));

        Member member = memberRepository.save(Member.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .photoUrl(dto.getPhotoUrl())
                .build());

        User user = User.builder()
                .email(dto.getEmail())
                .emailVerified(dto.isEmailVerified())
                .passwordHash(dto.getPassword() != null ? passwordEncoder.encode(dto.getPassword()) : null)
                .memberId(member.getId())
                .status("ACTIVE")
                .createdAt(OffsetDateTime.now())
                .userRoles(new HashSet<>())
                .socialAccounts(new HashSet<>())
                .build();

        user.getUserRoles().add(UserRole.builder().role(guestRole).build());

        log.info("Creating new user via {}", dto.getAuthProvider());
        emailService.sendWelcomeEmail(user.getEmail());
        auditService.logAction(user.getEmail(), "USER_REGISTRATION", "Registered via " + dto.getAuthProvider());

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

            auditService.logAction(user.getEmail(), "ACCOUNT_LINKED", "Linked " + dto.getAuthProvider());
        }
    }
}
