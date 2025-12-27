package com.shrimali.modules.auth.service.impl;

import com.shrimali.exceptions.BadRequestException;
import com.shrimali.model.Member;
import com.shrimali.model.auth.Otp;
import com.shrimali.model.auth.User;
import com.shrimali.model.enums.AuthProviderType;
import com.shrimali.model.enums.RoleName;
import com.shrimali.modules.auth.dto.*;
import com.shrimali.modules.auth.service.AuthService;
import com.shrimali.modules.auth.service.JwtUtil;
import com.shrimali.modules.auth.service.OtpService;
import com.shrimali.modules.auth.service.UserRegistrationService;
import com.shrimali.modules.shared.services.AppUtils;
import com.shrimali.modules.shared.services.EmailService;
import com.shrimali.repositories.MemberRepository;
import com.shrimali.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    private final UserRegistrationService userRegistrationService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        var optUser = userRepository.findByEmail(loginRequest.username());

        if (optUser.isEmpty()) {
            throw new BadCredentialsException("Invalid credentials");
        }

        User user = optUser.get();
        boolean matches = passwordEncoder.matches(loginRequest.password(), user.getPassword());
        if (!matches) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(
                new TokenResponse(token));
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        // uniqueness checks
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone already registered");
        }

        RegistrationDto dto = RegistrationDto.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(request.getPassword())
                .authProvider(AuthProviderType.LOCAL)
                .emailVerified(false)
                .build();

        User saved = userRegistrationService.registerOrUpdateUser(dto);

        return RegisterResponse.builder()
                .email(saved.getEmail())
                .phone(saved.getPhone())
                .token(jwtUtil.generateToken(saved.getEmail()))
                .build();
    }

    @Override
    public AuthResponse oauthLogin(User user) {
        log.info("OAuth login successful for user id={} email={}", user.getId(), user.getEmail());

        // 1. Update last login time (Optional, but good practice)
        user.setLastLoginAt(java.time.OffsetDateTime.now());
        userRepository.save(user);

        // 2. Generate token using the user's principal (email)
        String token = jwtUtil.generateToken(user.getEmail());

        // 3. Return the standard AuthResponse
        return new AuthResponse(new TokenResponse(token));
    }

    @Override
    public UserResponse userByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow();

        boolean profileCompleted = false;
        Member member = memberRepository.findById(user.getMemberId()).orElseThrow();
        if (member.getDob() != null) {
            profileCompleted = true;
        }

        return UserResponse.builder()
                .email(user.getEmail())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .firstName(member.getFirstName())
                .middleName(member.getMiddleName())
                .lastName(member.getLastName())
                .gender(member.getGender())
                .photoUrl(member.getPhotoUrl())
                .completed(profileCompleted)
                .build();
    }

    public static String generateMemberNumber(String lastName) {
        long ts = System.currentTimeMillis();
        int unique = Math.abs(Long.toString(ts).hashCode()) % 1_000_000;
        return lastName.toUpperCase() + "-" + String.format("%06d", unique);
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        Otp otp = otpService.generateOtp(user, "FORGOT_PASSWORD");
        emailService.sendForgotPasswordMail(email, otp.getTokenId(), otp.getCode());
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest request) {
        // 1️⃣ Validate reset token
        Otp otpEntity = otpService.validateOtp(request.tokenId(), request.token());

        // 2️⃣ Resolve user (recommended: via userId, fallback email)
        User user = userRepository.findById(otpEntity.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 3️⃣ Update password
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // 4️⃣ Consume token (one-time use)
        otpService.consumeOtp(otpEntity);
    }

    @Override
    public UserResponse me(Principal principal) {
        User user = getUser(principal);

        Member member = memberRepository
                .findById(user.getMemberId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        RoleName role = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority) // ROLE_ADMIN
                .map(RoleName::valueOf)               // enum
                .findFirst()
                .orElse(RoleName.ROLE_GUEST);

        return UserResponse.builder()
                .email(user.getEmail())
                .emailVerified(user.getEmailVerified())
                .phoneVerified(user.getPhoneVerified())
                .firstName(member.getFirstName())
                .middleName(member.getMiddleName())
                .lastName(member.getLastName())
                .gender(member.getGender())
                .photoUrl(member.getPhotoUrl())
                .phone(user.getPhone())
                .dob(member.getDob() != null ? member.getDob().toString() : null)
                .role(role)
                .completionPercentage(AppUtils.calculateCompletion(member))
                .build();
    }

    @Override
    public AuthResponse generateToken(Principal principal) {
        User user = getUser(principal);

        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(
                new TokenResponse(token));
    }

    private User getUser(Principal principal) {
        if(principal == null) {
            throw new BadRequestException("Invalid credentials");
        }
        return (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
    }
}
