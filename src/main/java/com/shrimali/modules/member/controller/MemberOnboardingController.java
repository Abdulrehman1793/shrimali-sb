package com.shrimali.modules.member.controller;

import com.shrimali.model.auth.User;
import com.shrimali.modules.member.dto.MemberDiscoveryDto;
import com.shrimali.modules.member.dto.MemberMatchResponse;
import com.shrimali.modules.member.services.MemberDiscoveryService;
import com.shrimali.modules.shared.services.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/members/onboarding")
@RequiredArgsConstructor
@Slf4j
@Validated
public class MemberOnboardingController {
    private final MemberDiscoveryService discoveryService;
    private final SecurityUtils securityUtils;

    /**
     * STEP 1: Discovery Search
     * Finds potential matches in the community tree based on user input.
     */
    @GetMapping("/discovery")
    public ResponseEntity<List<MemberMatchResponse>> discover(@ModelAttribute MemberDiscoveryDto dto) {
        return ResponseEntity.ok(discoveryService.findPotentialMatches(dto));
    }

    /**
     * STEP 2: Register New Profile
     * Called when no match is found or the user chooses to create a fresh record.
     * Sets status to PENDING_APPROVAL.
     */
    @PostMapping("/new")
    public ResponseEntity<Map<String, String>> registerNewMember(@Valid @RequestBody MemberDiscoveryDto dto) {
        User currentUser = securityUtils.getCurrentUser();

        log.info("New member registration request for: {} {}", dto.getFirstName(), dto.getLastName());

        discoveryService.registerNewMember(dto);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Registration successful. Awaiting community approval.");
        response.put("status", "AWAITING_COMMUNITY_APPROVAL");

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * STEP 2 (Alternative): Claim Existing Profile
     * Called when the user identifies an existing record as themselves ("This is Me").
     * Links the current User to the existing Member record.
     */
    @PostMapping("/claim/{memberId}")
    public ResponseEntity<Map<String, String>> claimProfile(@PathVariable Long memberId) {
        log.info("Profile claim request for Member ID: {} by User: {}",
                memberId, securityUtils.getCurrentUser().getUsername());

        discoveryService.claimProfile(memberId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Profile successfully claimed. Awaiting community approval.");
        response.put("status", "AWAITING_COMMUNITY_APPROVAL");

        return ResponseEntity.ok(response);
    }
}
