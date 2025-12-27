package com.shrimali.modules.auth.service.impl;

import com.shrimali.exceptions.BadRequestException;
import com.shrimali.model.auth.Otp;
import com.shrimali.model.auth.User;
import com.shrimali.modules.auth.service.OtpService;
import com.shrimali.modules.shared.services.TokenGenerator;
import com.shrimali.repositories.OtpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpServiceImpl implements OtpService {

    private final PasswordEncoder passwordEncoder;
    private final OtpRepository otpRepository;

    @Override
    public Otp generateOtp(User user, String purpose) {
        String tokenId = TokenGenerator.generateToken(12);   // short, public
        String rawToken = TokenGenerator.generateToken(32);  // secret

        String hashedToken = passwordEncoder.encode(rawToken);

        Otp otp = Otp.builder()
                .email(user.getEmail())
                .user(user)
                .tokenId(tokenId)
                .code(hashedToken)
                .purpose(purpose)
                .expiresAt(OffsetDateTime.now().plusMinutes(10))
                .build();

        otp = otpRepository.save(otp);

        // IMPORTANT: return RAW token to be emailed
        otp.setCode(rawToken);

        return otp;
    }

    @Override
    public Otp validateOtp(String tokenId, String rawToken) {
        Otp otp = otpRepository
                .findByTokenIdAndConsumedAtIsNull(tokenId)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset link"));

        if (otp.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BadRequestException("Reset link expired");
        }

        if (!passwordEncoder.matches(rawToken, otp.getCode())) {
            throw new BadRequestException("Invalid reset link");
        }

        return otp;
    }

    @Override
    public void consumeOtp(Otp otp) {
        otp.setConsumedAt(OffsetDateTime.now());
        otpRepository.save(otp);
    }
}
