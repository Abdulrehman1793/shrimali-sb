package com.shrimali.modules.member.controller;

import com.shrimali.modules.member.dto.MemberAccessPayload;
import com.shrimali.modules.member.services.MemberAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * Member Administration & Moderation Controller
 * * * Handles privileged operations restricted to Community Administrators
 * and Super Admins.
 * * Responsibilities: Member approvals, profile locking/unlocking,
 * identity verification, and role/access management.
 * * This controller acts as the 'Control Plane' for data integrity and
 * community standards.
 */
@RestController
@RequestMapping("/api/v1/members/admin")
@RequiredArgsConstructor
@Validated
public class MemberAdminController {
    private final MemberAdminService memberAdminService;

    @GetMapping("/guest/{memberId}/approve")
    public ResponseEntity<?> approveGuestUser(@PathVariable Long memberId) throws Exception {

        memberAdminService.approveGuestUser(memberId);

        return ResponseEntity.ok(Map.of("message", "success", "data", "Guest member has been approved"));
    }

    @PostMapping("/user/admin")
    public ResponseEntity<?> updateUserAdminAccess(@RequestBody MemberAccessPayload accessPayload) {

        memberAdminService.updateMemberAccess(accessPayload);

        String message;

        if (Boolean.TRUE.equals(accessPayload.approved())) {
            message = "Admin role assigned successfully";
        } else if (Boolean.TRUE.equals(accessPayload.remove())) {
            message = "Admin role revoked successfully";
        } else {
            message = "Member access updated successfully";
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", message,
                        "status", "SUCCESS"
                )
        );
    }


    @GetMapping("/user/{memberId}/remove")
    public ResponseEntity<?> removeUserFromCommunity(@PathVariable Long memberId) throws Exception {

        memberAdminService.removeUserFromCommunity(memberId);

        return ResponseEntity.ok(Map.of("message", "success", "data", "Current member has been removed from community"));
    }
}
