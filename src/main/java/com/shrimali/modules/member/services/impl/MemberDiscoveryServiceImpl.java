package com.shrimali.modules.member.services.impl;

import com.shrimali.model.auth.User;
import com.shrimali.model.enums.UserStatus;
import com.shrimali.model.member.Member;
import com.shrimali.modules.member.dto.MemberDiscoveryDto;
import com.shrimali.modules.member.dto.MemberMatchResponse;
import com.shrimali.modules.member.services.MemberDiscoveryService;
import com.shrimali.modules.shared.services.AuditService;
import com.shrimali.modules.shared.services.SecurityUtils;
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
    private final AuditService auditService;
    private final SecurityUtils securityUtils;

    public List<MemberMatchResponse> findPotentialMatches(MemberDiscoveryDto dto) {
        List<Member> members = memberRepository.findUnclaimedMatches(
                dto.getFirstName(), dto.getLastName(), dto.getDob()
        );

        return members.stream().map(m -> MemberMatchResponse.builder()
                        .memberId(m.getId())
                        .fullName(m.getFirstName() + " " + m.getLastName())
                        .fatherName(m.getFather() != null ? m.getFather().getFirstName() : "Unknown")
                        .paternalVillage(m.getPaternalVillage())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void registerNewMember(MemberDiscoveryDto dto) {
        User currentUser = securityUtils.getCurrentUser(); // Helper to get logged-in user

        // 1. Create the Member Profile
        Member newMember = Member.builder()
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .dob(dto.getDob())
                .paternalVillage(dto.getPaternalVillage())
                .gender(dto.getGender())
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
}
