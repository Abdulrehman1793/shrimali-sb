package com.shrimali.repositories;

import com.shrimali.model.auth.Otp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findTopByEmailAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
            String email, String purpose
    );

    Optional<Otp> findTopByPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(String purpose);

    Optional<Otp> findByTokenIdAndConsumedAtIsNull(String tokenId);
}

