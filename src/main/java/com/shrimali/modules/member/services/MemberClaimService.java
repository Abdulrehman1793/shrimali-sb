package com.shrimali.modules.member.services;

import com.shrimali.modules.member.dto.ActiveClaimResponse;
import com.shrimali.modules.member.dto.ClaimActionRequest;
import com.shrimali.modules.member.dto.UpdateClaimRequest;

public interface MemberClaimService {
    ActiveClaimResponse getActiveClaim();

    void updateClaim(Long claimId, UpdateClaimRequest request);

    void performAction(Long claimId, ClaimActionRequest request);
}
