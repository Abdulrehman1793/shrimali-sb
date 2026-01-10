package com.shrimali.modules.member.services.impl;

import com.shrimali.dto.AuthenticatedIdentity;
import com.shrimali.exceptions.BadRequestException;
import com.shrimali.model.auth.User;
import com.shrimali.model.enums.ClaimStatus;
import com.shrimali.model.member.ClaimActionLog;
import com.shrimali.model.member.Member;
import com.shrimali.model.member.MemberClaim;
import com.shrimali.modules.member.dto.ActiveClaimResponse;
import com.shrimali.modules.member.dto.ClaimActionRequest;
import com.shrimali.modules.member.dto.ClaimActionType;
import com.shrimali.modules.member.dto.UpdateClaimRequest;
import com.shrimali.modules.member.services.MemberClaimService;
import com.shrimali.modules.shared.services.EmailService;
import com.shrimali.modules.shared.services.SecurityUtils;
import com.shrimali.repositories.ClaimActionLogRepository;
import com.shrimali.repositories.member.MemberClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberClaimServiceImpl implements MemberClaimService {
    private final MemberClaimRepository memberClaimRepository;
    private final ClaimActionLogRepository claimActionLogRepository;

    private final EmailService emailService;
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
                .lastReminderSentAt(memberClaim.getLastReminderSentAt())
                .reminderCount(memberClaim.getReminderCount())

                // ───────────── Target Member (READ-ONLY) ─────────────
                .targetMember(
                        ActiveClaimResponse.TargetMember.builder()
                                .id(targetMember.getId())
                                .fullName(fullName)
                                .photoUrl(targetMember.getThumbnailUrl())
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
        memberClaim.setIdentityNote(request.getNote());

        boolean canSendReminder = false;
        if (memberClaim.getLastReminderSentAt() == null) {
            canSendReminder = true;
        } else {
            LocalDateTime nextAllowed =
                    memberClaim.getLastReminderSentAt().plusHours(24);

            if (LocalDateTime.now().isAfter(nextAllowed)) {
                canSendReminder = true;
            }
        }
        if (canSendReminder) {

            String targetFullName = String.format("%s %s %s",
                    memberClaim.getTargetMember().getFirstName(),
                    memberClaim.getTargetMember().getMiddleName(),
                    memberClaim.getTargetMember().getLastName()
            ).replaceAll("\\s+", " ").trim();

            emailService.sendClaimRequestEmail(
                    memberClaim.getRequester().getEmail(), // or currentOwner if required
                    memberClaim.getRequesterFullName(),
                    targetFullName,
                    claimId
            );

            memberClaim.setLastReminderSentAt(LocalDateTime.now());
            memberClaim.setReminderCount(
                    memberClaim.getReminderCount() == null
                            ? 1
                            : memberClaim.getReminderCount() + 1
            );
        }
        memberClaimRepository.save(memberClaim);
    }

    @Override
    @Transactional
    public void performAction(Long claimId, ClaimActionRequest request) {
        MemberClaim claim = memberClaimRepository.findById(claimId)
                .orElseThrow(() -> new BadRequestException("Claim not found"));

        User currentUser = securityUtils.getCurrentUser();

        switch (request.getAction()) {

            case WITHDRAW -> withdrawClaim(claim, currentUser);

            case SEND_REMINDER -> sendReminder(claim);

            case APPROVE -> logApproval(claim, currentUser, request.getReason());

            case REJECT -> logRejection(claim, currentUser, request.getReason());

            default -> throw new BadRequestException("Unsupported action");
        }
    }

    private void withdrawClaim(MemberClaim claim, User user) {
        if (!claim.getRequester().getId().equals(user.getId())) {
            throw new BadRequestException("Not allowed");
        }

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new BadRequestException("Only pending claims can be withdrawn");
        }

        claim.setStatus(ClaimStatus.WITHDRAWN);
    }

    private void sendReminder(MemberClaim claim) {
        if (claim.getLastReminderSentAt() != null &&
                claim.getLastReminderSentAt().plusHours(24).isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Reminder already sent within 24 hours");
        }

        String targetFullName = String.format("%s %s %s",
                claim.getTargetMember().getFirstName(),
                claim.getTargetMember().getMiddleName(),
                claim.getTargetMember().getLastName()
        ).replaceAll("\\s+", " ").trim();

        emailService.sendClaimReminderEmail(
                securityUtils.getCurrentUser().getEmail(),
                claim.getRequesterFullName(), targetFullName, claim.getId());
        claim.setLastReminderSentAt(LocalDateTime.now());
        if (claim.getReminderCount() != null) {
            claim.setReminderCount(claim.getReminderCount() + 1);
        } else {
            claim.setReminderCount(1);
        }
    }


    private void logApproval(
            MemberClaim claim,
            User user,
            String reason
    ) {
        claimActionLogRepository.save(
                ClaimActionLog.builder()
                        .claim(claim)
                        .action(ClaimActionType.APPROVE)
                        .performedBy(user)
                        .note(reason)
                        .performedAt(LocalDateTime.now())
                        .build()
        );
    }

    private void logRejection(
            MemberClaim claim,
            User user,
            String reason
    ) {
        claimActionLogRepository.save(
                ClaimActionLog.builder()
                        .claim(claim)
                        .action(ClaimActionType.REJECT)
                        .performedBy(user)
                        .note(reason)
                        .performedAt(LocalDateTime.now())
                        .build()
        );
    }
}
