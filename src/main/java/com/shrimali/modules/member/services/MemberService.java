package com.shrimali.modules.member.services;

import com.shrimali.dto.PagedResponse;
import com.shrimali.model.member.Member;
import com.shrimali.modules.member.dto.*;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface MemberService {
    PagedResponse<MemberListItem> findAll(String q, Pageable pageable);

    PagedResponse<MemberListItem> listMembers(String q, Pageable pageable);

    PagedResponse<MemberListItem> guests(String q, Pageable pageable);

    Member getById(Long id);

    MemberResponse me(Principal principal);

    MemberResponse completeProfile(Principal principal, MemberPayload member);

    void updateUserProfile(Principal principal, UserProfile userProfile);

    void approveGuestUser(Principal principal, Long memberId);

    void updateMemberAccess(Principal principal, MemberAccessPayload accessPayload);

    void removeUserFromCommunity(Principal principal, Long memberId);

    MemberProfileResponse getCurrentMemberProfile(Principal principal);

    void updateBasicInfo(Principal principal, BasicInfoDTO basicInfo);

    void updateFatherDetails(Principal principal, DiscoverySearchRequest dto);

    void updateMotherDetails(Principal principal, MotherDetailsDTO dto);

    void updateSpouseDetails(Principal principal, SpouseDetailsDTO dto);

    DiscoveryResponse discoverExistingMember(Principal principal, DiscoverySearchRequest request);
}
