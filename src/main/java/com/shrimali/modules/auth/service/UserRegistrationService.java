package com.shrimali.modules.auth.service;

import com.shrimali.model.Member;
import com.shrimali.model.auth.Role;
import com.shrimali.model.auth.User;
import com.shrimali.model.auth.UserRole;
import com.shrimali.model.enums.RoleName;
import com.shrimali.modules.auth.dto.RegistrationDto;
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
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationService {
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;

    @Transactional
    public User registerOrUpdateUser(RegistrationDto dto) {
        // 1. Check for existing user
        Optional<User> existingUser = userRepository.findByEmail(dto.getEmail());
        Role guestRole = roleRepository.findByName(RoleName.ROLE_GUEST)
                .orElseThrow(() -> new RuntimeException("Default Role not found"));

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            updateExistingUser(user, dto);
            return userRepository.save(user);
        }

        // 2. Create and Save Member first (Foreign Key constraint)
        Member member = Member.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .middleName(dto.getMiddleName())
                .gender(dto.getGender())
                .photoUrl(dto.getPhotoUrl())
                .dob(dto.getDob())
                .build();
        member = memberRepository.save(member);

        // 3. Create User linked to Member
        User user = User.builder()
                .email(dto.getEmail())
                .emailVerified(dto.isEmailVerified())
                .phone(dto.getPhone())
                .passwordHash(dto.getPassword() != null ? passwordEncoder.encode(dto.getPassword()) : null)
                .memberId(member.getId())
                .status("ACTIVE")
                .authProvider(dto.getAuthProvider())
                .lastLoginAt(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .userRoles(Set.of(UserRole.builder().role(guestRole).build()))
                .build();

        emailService.sendWelcomeEmail(user.getEmail());

        return userRepository.save(user);
    }

    private void updateExistingUser(User user, RegistrationDto dto) {
        user.setLastLoginAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        if (user.getAuthProvider() == null) {
            user.setAuthProvider(dto.getAuthProvider());
        }
    }
}
