package com.shrimali.modules.member.services.impl;

import com.shrimali.exceptions.BadRequestException;
import com.shrimali.exceptions.ConflictException;
import com.shrimali.model.Gotra;
import com.shrimali.model.auth.Role;
import com.shrimali.model.auth.User;
import com.shrimali.model.enums.*;
import com.shrimali.model.member.Member;
import com.shrimali.model.member.MemberClaim;
import com.shrimali.modules.member.dto.MemberDiscoveryDto;
import com.shrimali.modules.member.dto.MemberMatchResponse;
import com.shrimali.modules.member.services.MemberDiscoveryService;
import com.shrimali.modules.shared.services.AuditService;
import com.shrimali.modules.shared.services.SecurityUtils;
import com.shrimali.repositories.GotraRepository;
import com.shrimali.repositories.MemberRepository;
import com.shrimali.repositories.UserRepository;
import com.shrimali.repositories.member.MemberClaimRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberDiscoveryServiceImpl implements MemberDiscoveryService {
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final GotraRepository gotraRepository;
    private final MemberClaimRepository memberClaimRepository;

    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    public List<MemberMatchResponse> findPotentialMatches(MemberDiscoveryDto dto) {
        List<Member> members = memberRepository.findUnclaimedMatches(
                dto.getFirstName(), dto.getLastName(), dto.getDob()
        );

        return members.stream().map(m -> MemberMatchResponse.builder()
                        .memberId(m.getId())
                        .firstName(m.getFirstName())
                        .middleName(m.getMiddleName())
                        .lastName(m.getLastName())
                        .gender(String.valueOf(m.getGender()))
                        .photoUrl(m.getPhotoUrl())
                        .dob(dto.getDob())
                        .gotra(m.getPaternalGotra().getName())
                        .paternalVillage(m.getPaternalVillage())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Member registerNewMember(MemberDiscoveryDto dto) {
        User currentUser = securityUtils.getCurrentUser();

        boolean alreadyExists = memberRepository.existsByFirstNameIgnoreCaseAndMiddleNameIgnoreCaseAndLastNameIgnoreCaseAndDob(
                dto.getFirstName(),
                dto.getMiddleName(),
                dto.getLastName(),
                dto.getDob()
        );

        if (alreadyExists) {
            throw new ConflictException("A profile with this name and date of birth already exists in the community tree.");
        }

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a ->
                        Objects.requireNonNull(a.getAuthority()).equalsIgnoreCase(RoleName.ROLE_ADMIN.toString())
                                || a.getAuthority().equalsIgnoreCase(RoleName.ROLE_SUPER_ADMIN.toString()));

        MembershipStatus initialMembershipStatus = isAdmin ? MembershipStatus.ACTIVE : MembershipStatus.PENDING_APPROVAL;
        ProfileStatus initialProfileStatus = isAdmin ? ProfileStatus.VERIFIED : ProfileStatus.DRAFT;

        Gotra gotra = gotraRepository.findById(dto.getGotra())
                .orElseThrow(() -> new BadRequestException("Gotra not found"));

        Member newMember = Member.builder()
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .membershipNumber(Member.generateMemberNumber(dto.getFirstName(), dto.getMiddleName(), dto.getLastName()))
                .dob(dto.getDob())
                .paternalVillage(dto.getPaternalVillage())
                .naniyalVillage(dto.getNaniyalVillage())
                .gender(dto.getGender())
                .paternalGotra(gotra)
                .owner(currentUser)
                .linkedUser(currentUser)
                .membershipStatus(initialMembershipStatus)
                .status(initialProfileStatus)
                .verified(isAdmin) // Admins are auto-verified
                .verifiedAt(isAdmin ? OffsetDateTime.now() : null)
                .verifiedBy(isAdmin ? currentUser : null)
                .build();

        Member savedMember = memberRepository.save(newMember);

        // 2. Link User to Member and update Status
        currentUser.setMemberId(savedMember.getId());
        currentUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(currentUser);

        auditService.logAction("NEW_MEMBER_REQUEST",
                "User requested new profile creation. Awaiting admin approval.");

        return savedMember;
    }

    @Override
    @Transactional
    public void claimProfile(Long memberId) {
        User currentUser = securityUtils.getCurrentUser();

        // Find the member record
        Member existingMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BadRequestException("Profile not found"));

        // Already claimed
        if (existingMember.getLinkedUser() != null) {
            throw new BadRequestException("This profile has already been claimed.");
        }

        // Deceased cannot be claimed
        if (existingMember.isDeceased()) {
            throw new BadRequestException("A profile marked as deceased cannot be claimed.");
        }

        // Prevent duplicate pending claims
        boolean alreadyPending = memberClaimRepository
                .existsByTargetMemberAndStatus(existingMember, ClaimStatus.PENDING);

        if (alreadyPending) {
            throw new BadRequestException("A claim request is already pending for this profile.");
        }

        // 1. Link the profile to the current user
//        existingMember.setLinkedUser(currentUser);
//        existingMember.setOwner(currentUser);

        MemberClaim memberClaim = MemberClaim.builder()
                .targetMember(existingMember)
                .currentOwner(existingMember.getOwner())
                .requester(currentUser)
                .status(ClaimStatus.PENDING)
                .build();

        memberClaimRepository.save(memberClaim);

//        existingMember.setMembershipStatus(MembershipStatus.PENDING_APPROVAL);
        memberRepository.save(existingMember);

//        currentUser.setMemberId(existingMember.getId());
        currentUser.setStatus(UserStatus.ACTIVE);
        userRepository.save(currentUser);

        auditService.logAction("PROFILE_CLAIM_REQUEST", "User claimed existing profile ID: " + memberId);
    }
}
