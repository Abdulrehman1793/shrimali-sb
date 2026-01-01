package com.shrimali.modules.member.controller;

import com.shrimali.dto.PagedResponse;
import com.shrimali.modules.member.dto.*;
import com.shrimali.modules.member.services.MemberSearchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Member Search & Directory Controller
 * * Handles all read-only exploration of the community.
 * This controller is the primary entry point for users to find relatives
 * and for admins to manage the approval queue.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Validated
public class MemberSearchController {
    private final MemberSearchService memberSearchService;

    /**
     * Global Directory Search
     * Performs a high-level search across the community directory.
     * Typically used for the main search bar.
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<MemberListItem>> searchDirectory(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(value = "size", defaultValue = "50") @Min(1) int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(memberSearchService.search(q, pageable));
    }

    /**
     * Paginated Member Browsing
     * Returns a standard list of members. Usually used for the "Explore"
     * or "All Members" tab with default sorting.
     */
    @PreAuthorize("hasAnyRole('USER','ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<PagedResponse<MemberListItem>> getMembersPaged(
            // Spring maps ?village=xyz&gotra=abc automatically to this object
            MemberFilterRequest filters,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) int size) {

        final Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // Pass the filter object to your service layer
        return ResponseEntity.ok(memberSearchService.listMembers(filters, pageable));
    }

    /**
     * Admin Approval Queue
     * Retrieves a list of 'Guest' profiles or profiles pending verification.
     * Note: In a final refactor, this endpoint should move to MemberAdminController.
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/pending-approvals")
    public ResponseEntity<PagedResponse<MemberListItem>> getPendingApprovals(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) int size) {

        final Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(memberSearchService.findPendingMembers(q, pageable));
    }

    /**
     * Retrieves full details of a specific community member.
     * This is used when clicking on a member's card in the directory.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long id) {
        return ResponseEntity.ok(memberSearchService.getMember(id));
    }

    /**
     * Initiates the 'Tree Discovery' process.
     * Searches for existing members that match the provided criteria to prevent duplicates.
     */
    @PostMapping("/discovery")
    public ResponseEntity<DiscoveryResponse> discoverMember(@Valid @RequestBody DiscoverySearchRequest request) {
        log.info("Initiating member discovery for: {} {} ({})",
                request.firstName(), request.lastName(), request.relationType());

        // Service logic moved to memberSearchService
        DiscoveryResponse response = memberSearchService.discoverExistingMember(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all member profiles managed by the current user.
     * These are profiles created by the user for their family tree
     * where they hold ownership and editing rights.
     */
    @GetMapping("/managed")
    public ResponseEntity<PagedResponse<MemberListItem>> getManagedMembers(
            @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) int size
    ) {
        // We delegate to the service which uses SecurityUtils to find 'who' is asking
        return ResponseEntity.ok(memberSearchService.getManagedMembers(page, size));
    }
}
