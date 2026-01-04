package com.shrimali.modules.member.services.impl;

import com.shrimali.model.auth.Role;
import com.shrimali.model.auth.User;
import com.shrimali.model.enums.MembershipStatus;
import com.shrimali.model.enums.RoleName;
import com.shrimali.model.member.Member;
import com.shrimali.modules.member.dto.MemberAccessPayload;
import com.shrimali.modules.member.services.MemberAdminService;
import com.shrimali.modules.member.services.MemberCommonService;
import com.shrimali.modules.shared.services.SecurityUtils;
import com.shrimali.repositories.MemberRepository;
import com.shrimali.repositories.RoleRepository;
import com.shrimali.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class MemberAdminServiceImpl implements MemberAdminService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final SecurityUtils securityUtils;
    private final MemberCommonService commonService;

    @Override
    @Transactional
    public void approveGuestUser(Long memberId) {
        User approvedBy = securityUtils.getCurrentUser();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("Member not found with id: " + memberId));

        User guestUser = userRepository.findByMemberId(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("User not found for member id: " + memberId));

        if (!commonService.hasRole(guestUser, RoleName.ROLE_GUEST)) {
            throw new IllegalStateException("Only guest users can be approved");
        }

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() ->
                        new IllegalStateException("ROLE_USER not found"));

        // Role transition
        commonService.removeRole(guestUser, RoleName.ROLE_GUEST);
        commonService.addRoleIfMissing(guestUser, userRole);

        // Member update
        member.setApprovedBy(approvedBy);
        member.setApprovedAt(OffsetDateTime.now());
        member.setMembershipStatus(MembershipStatus.ACTIVE);

        userRepository.save(guestUser);
        memberRepository.save(member);
    }

    @Override
    public void removeUserFromCommunity(Long memberId) {
        User actionBy = securityUtils.getCurrentUser();

        // 1️⃣ Fetch member
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("Member not found with id: " + memberId));

        // 2️⃣ Fetch linked user
        User targetUser = userRepository.findByMemberId(memberId)
                .orElseThrow(() ->
                        new NoSuchElementException("User not found for member id: " + memberId));

        // 3️⃣ Validate community membership
        boolean isCommunityUser = commonService.hasRole(targetUser, RoleName.ROLE_USER) ||
                commonService.hasRole(targetUser, RoleName.ROLE_GUEST);

        if (!isCommunityUser) {
            throw new IllegalStateException("User does not belong to the community");
        }

        // 4️⃣ Fetch OUTSIDER role
        Role outsiderRole = roleRepository.findByName(RoleName.ROLE_OUTSIDER)
                .orElseThrow(() ->
                        new IllegalStateException("ROLE_OUTSIDER not found"));

        // 5️⃣ Remove community roles
        commonService.removeRole(targetUser, RoleName.ROLE_USER);
        commonService.removeRole(targetUser, RoleName.ROLE_GUEST);

        // 6️⃣ Assign OUTSIDER role if missing
        commonService.addRoleIfMissing(targetUser, outsiderRole);

        // 7️⃣ Update member status
        member.setApprovedBy(actionBy);
        member.setMembershipStatus(MembershipStatus.REJECTED);

        // 8️⃣ Persist
        userRepository.save(targetUser);
        memberRepository.save(member);
    }

    @Override
    public void updateMemberAccess(MemberAccessPayload accessPayload) {

    }
}
