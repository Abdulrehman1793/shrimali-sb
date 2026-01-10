package com.shrimali.modules.member.controller;

import com.shrimali.modules.member.dto.ActiveClaimResponse;
import com.shrimali.modules.member.dto.UpdateClaimRequest;
import com.shrimali.modules.member.services.MemberClaimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/me/claims")
@RequiredArgsConstructor
public class MemberClaimController {

    private final MemberClaimService memberClaimService;

    @GetMapping("/active")
    public ActiveClaimResponse getActiveClaim() {
        return memberClaimService.getActiveClaim();
    }

    @PatchMapping("/{claimId}")
    public void updateClaim(
            @PathVariable Long claimId, @RequestBody UpdateClaimRequest request) {
        memberClaimService.updateClaim(claimId, request);
    }
}
