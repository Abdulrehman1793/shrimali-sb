package com.shrimali.modules.member.controller;

import com.shrimali.modules.member.dto.BasicInfoDTO;
import com.shrimali.modules.member.dto.DiscoverySearchRequest;
import com.shrimali.modules.member.dto.MemberProfileResponse;
import com.shrimali.modules.member.dto.MemberResponse;
import com.shrimali.modules.member.services.MemberProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Member Profile Management Controller
 * * Focuses on "Self-Service" operations for the authenticated member.
 * Includes profile completion, identity updates (before verification/lock),
 * and managing direct relationship links (Father/Mother/Spouse).
 * * Access Control: Most endpoints require the 'OWNER' relationship or
 * specific permissions checked via {@link com.shrimali.modules.shared.services.SecurityUtils}.
 */
@RestController
@RequestMapping("/api/v1/members/profile")
@RequiredArgsConstructor
@Validated
public class MemberProfileController {
    private final MemberProfileService memberProfileService;

    /**
     * Retrieves the deep profile data for the current member.
     * Accessible via: GET /api/v1/members/profile
     */
    @GetMapping
    public ResponseEntity<MemberProfileResponse> getMyProfile() {
        return ResponseEntity.ok(memberProfileService.getCurrentMemberProfile());
    }

    /**
     * Gets a lightweight account summary.
     * Useful for the frontend navbar/sidebar to show name and avatar.
     * Accessible via: GET /api/v1/members/profile/account
     */
    @GetMapping("/account")
    public ResponseEntity<MemberResponse> getAccountSummary() {
        return ResponseEntity.ok(memberProfileService.me());
    }

    /**
     * Updates the authenticated user's core identity details.
     */
    @PutMapping("/basic-info")
    public ResponseEntity<Void> updateBasicInfo(@Valid @RequestBody BasicInfoDTO dto) {
        memberProfileService.updateBasicInfo(dto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Links an existing member or creates a new 'Ghost' father profile.
     * The current member becomes the 'Owner' of the newly created profile.
     */
    @PutMapping("/father")
    public ResponseEntity<Void> updateFatherDetails(@RequestBody DiscoverySearchRequest dto) {
        memberProfileService.updateFatherDetails(dto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Links an existing member or creates a new 'Ghost' mother profile.
     * The current member becomes the 'Owner' of the newly created profile.
     */
    @PutMapping("/mother")
    public ResponseEntity<Void> updateMotherDetails(@RequestBody DiscoverySearchRequest dto) {
        memberProfileService.updateMotherDetails(dto);
        return ResponseEntity.noContent().build();
    }

    /**
     * Links an existing member or creates a new 'Ghost' spouse profile.
     * The current member becomes the 'Owner' of the newly created profile.
     */
    @PutMapping("/spouse")
    public ResponseEntity<Void> updateSpouseDetails(@RequestBody DiscoverySearchRequest dto) {
        memberProfileService.updateSpouseDetails(dto);
        return ResponseEntity.noContent().build();
    }
}
