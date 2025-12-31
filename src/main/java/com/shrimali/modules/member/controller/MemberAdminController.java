package com.shrimali.modules.member.controller;

import com.shrimali.modules.member.services.MemberAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Validated
public class MemberAdminController {
    private final MemberAdminService memberAdminService;
}
