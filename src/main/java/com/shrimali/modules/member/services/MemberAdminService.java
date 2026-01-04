package com.shrimali.modules.member.services;

import com.shrimali.modules.member.dto.MemberAccessPayload;

import java.security.Principal;

public interface MemberAdminService {
    void approveGuestUser(Long memberId);

    void updateMemberAccess(MemberAccessPayload accessPayload);

    void removeUserFromCommunity(Long memberId);
}
