package com.shrimali.modules.member.services;

import com.shrimali.model.Member;
import com.shrimali.modules.member.dto.MemberDiscoveryDto;
import com.shrimali.modules.member.dto.MemberMatchResponse;

import java.util.List;

public interface MemberDiscoveryService {
    /**
     * Finds unclaimed profiles that match the user's basic identity.
     */
    List<MemberMatchResponse> findPotentialMatches(MemberDiscoveryDto dto);

    /**
     * Creates a brand-new member record and sets the user to "Waiting Room" status.
     */
    void registerNewMember(MemberDiscoveryDto dto);
}
