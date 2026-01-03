package com.shrimali.modules.member.services.impl;

import com.shrimali.exceptions.BadRequestException;
import com.shrimali.exceptions.ConflictException;
import com.shrimali.model.Gotra;
import com.shrimali.model.auth.User;
import com.shrimali.model.enums.UserStatus;
import com.shrimali.model.member.Member;
import com.shrimali.modules.member.dto.MemberDiscoveryDto;
import com.shrimali.modules.member.dto.MemberMatchResponse;
import com.shrimali.modules.member.services.MemberDiscoveryService;
import com.shrimali.modules.shared.services.AuditService;
import com.shrimali.modules.shared.services.SecurityUtils;
import com.shrimali.repositories.GotraRepository;
import com.shrimali.repositories.MemberRepository;
import com.shrimali.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberDiscoveryServiceImpl implements MemberDiscoveryService {
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final GotraRepository gotraRepository;

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
    public void registerNewMember(MemberDiscoveryDto dto) {
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

        Gotra gotra = gotraRepository.findById(dto.getGotra())
                .orElseThrow(() -> new BadRequestException("Gotra not found"));

        // 1. Create the Member Profile
        Member newMember = Member.builder()
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .dob(dto.getDob())
                .paternalVillage(dto.getPaternalVillage())
                .naniyalVillage(dto.getNaniyalVillage())
                .gender(dto.getGender())
                .paternalGotra(gotra)
                .owner(currentUser)      // User manages themselves
                .linkedUser(currentUser) // User IS this person
                .membershipStatus("PENDING_APPROVAL") // The Gatekeeper
                .build();

        Member savedMember = memberRepository.save(newMember);

        // 2. Link User to Member and update Status
        currentUser.setMemberId(savedMember.getId());
        currentUser.setStatus(UserStatus.AWAITING_COMMUNITY_APPROVAL);
        userRepository.save(currentUser);

        auditService.logAction("NEW_MEMBER_REQUEST",
                "User requested new profile creation. Awaiting admin approval.");
    }

    @Override
    @Transactional
    public void claimProfile(Long memberId) {
        User currentUser = securityUtils.getCurrentUser();

        // Find the member record
        Member existingMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new BadRequestException("Profile not found"));

        // Security Check: Ensure profile isn't already claimed by someone else
        if (existingMember.getLinkedUser() != null) {
            throw new BadRequestException("This profile has already been claimed.");
        }

        // 1. Link the profile to the current user
        existingMember.setLinkedUser(currentUser);
        existingMember.setOwner(currentUser);
        existingMember.setMembershipStatus("PENDING_APPROVAL"); // Still requires admin check
        memberRepository.save(existingMember);

        // 2. Update User record
        currentUser.setMemberId(existingMember.getId());
        currentUser.setStatus(UserStatus.AWAITING_COMMUNITY_APPROVAL);
        userRepository.save(currentUser);

        auditService.logAction("PROFILE_CLAIM_REQUEST",
                "User claimed existing profile ID: " + memberId);
    }
}
