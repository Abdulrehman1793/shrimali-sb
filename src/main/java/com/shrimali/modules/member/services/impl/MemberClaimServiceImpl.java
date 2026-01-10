package com.shrimali.modules.member.services.impl;

import com.shrimali.exceptions.BadRequestException;
import com.shrimali.model.auth.User;
import com.shrimali.model.enums.ClaimStatus;
import com.shrimali.model.member.Member;
import com.shrimali.model.member.MemberClaim;
import com.shrimali.modules.member.dto.ActiveClaimResponse;
import com.shrimali.modules.member.dto.UpdateClaimRequest;
import com.shrimali.modules.member.services.MemberClaimService;
import com.shrimali.modules.shared.services.SecurityUtils;
import com.shrimali.repositories.member.MemberClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberClaimServiceImpl implements MemberClaimService {
    private final MemberClaimRepository memberClaimRepository;

    private final SecurityUtils securityUtils;

    @Override
    public ActiveClaimResponse getActiveClaim() {

        User currentUser = securityUtils.getCurrentUser();

        MemberClaim memberClaim = memberClaimRepository
                .findTopByRequesterAndStatusOrderByCreatedAtDesc(
                        currentUser,
                        ClaimStatus.PENDING
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Active member claim not found"
                        )
                );

        Member targetMember = memberClaim.getTargetMember();

        String fullName = Stream.of(
                        targetMember.getFirstName(),
                        targetMember.getMiddleName(),
                        targetMember.getLastName()
                )
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" "));

        return ActiveClaimResponse.builder()
                .claimId(memberClaim.getId())
                .status(memberClaim.getStatus().name())
                .requestedAt(memberClaim.getCreatedAt())

                // ───────────── Target Member (READ-ONLY) ─────────────
                .targetMember(
                        ActiveClaimResponse.TargetMember.builder()
                                .id(targetMember.getId())
                                .fullName(fullName)
                                .photoUrl(targetMember.getPhotoUrl())
                                .relation(null) // optional if you later store relation
                                .build()
                )

                // ───────────── Requester Temporary Data ─────────────
                .requesterFullName(memberClaim.getRequesterFullName())
                .requesterRelation(memberClaim.getRequesterRelation())
                .requesterPhotoUrl(memberClaim.getRequesterPhotoUrl())
                .requesterThumbnailUrl(memberClaim.getRequesterThumbnailUrl())
                .note(memberClaim.getIdentityNote())

                .build();
    }

    @Override
    @Transactional
    public void updateClaim(Long claimId, UpdateClaimRequest request) {
        MemberClaim memberClaim = memberClaimRepository.findById(claimId)
                .orElseThrow(() -> new BadRequestException("Claim not found"));

        memberClaim.setRequesterFullName(request.getRequesterFullName());
        memberClaim.setRequesterRelation(request.getRequesterRelation());
        memberClaim.setRequesterPhotoUrl(request.getRequesterPhotoUrl());
        memberClaim.setRequesterThumbnailUrl(request.getRequesterThumbnailUrl());

        memberClaim.setIdentityNote(request.getNote());
    }
}
