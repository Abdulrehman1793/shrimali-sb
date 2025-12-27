package com.shrimali.modules.member.controller;

import com.shrimali.dto.PagedResponse;
import com.shrimali.model.Member;
import com.shrimali.modules.member.dto.*;
import com.shrimali.modules.member.mapper.MemberMapper;
import com.shrimali.modules.member.services.MemberService;
import com.shrimali.modules.shared.services.ImageUploadService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Slf4j
@Validated
public class MemberController {
    private final ImageUploadService imageUploadService;
    private final MemberService memberService;

    private final MemberMapper memberMapper;

    @GetMapping("/all")
    public ResponseEntity<?> findAll(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(value = "size", defaultValue = "50") @Min(1) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PagedResponse<MemberListItem> pageResult = memberService.findAll(q, pageable);

        return ResponseEntity.ok(pageResult);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<?> listMembers(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PagedResponse<MemberListItem> pageResult = memberService.listMembers(q, pageable);

        return ResponseEntity.ok(pageResult);
    }

    @GetMapping("/guests")
    public ResponseEntity<?> listGuest(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(value = "size", defaultValue = "20") @Min(1) int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PagedResponse<MemberListItem> pageResult = memberService.guests(q, pageable);

        return ResponseEntity.ok(pageResult);
    }

    /**
     * Get single member by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long id) {
        try {
            Member m = memberService.getById(id);
            return ResponseEntity.ok(memberMapper.toDto(m));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Principal principal) {
        MemberResponse result = memberService.me(principal);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/profile/complete")
    public ResponseEntity<?> completeProfile(
            Principal principal, @RequestBody MemberPayload memberPayload) throws Exception {
        MemberResponse result = memberService.completeProfile(principal, memberPayload);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/profile/update")
    public ResponseEntity<?> updateUserProfile(
            Principal principal, @RequestBody UserProfile userProfile) throws Exception {
        memberService.updateUserProfile(principal, userProfile);
        return ResponseEntity.ok(Map.of("message", "success", "data", "Profile updated successfully"));
    }

    @PostMapping("/profile/add/contact")
    public ResponseEntity<?> updateAddContact(
            Principal principal, @RequestBody ContactPayload contactPayload) throws Exception {

        return ResponseEntity.ok(Map.of("message", "success", "data", "New Contact added successfully"));
    }

    @GetMapping("/guest/{memberId}/approve")
    public ResponseEntity<?> approveGuestUser(
            Principal principal, @PathVariable Long memberId) throws Exception {

        memberService.approveGuestUser(principal, memberId);

        return ResponseEntity.ok(Map.of("message", "success", "data", "Guest member has been approved"));
    }

    @PostMapping("/user/admin")
    public ResponseEntity<?> updateUserAdminAccess(
            Principal principal,
            @RequestBody MemberAccessPayload accessPayload) {

        memberService.updateMemberAccess(principal, accessPayload);

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
    public ResponseEntity<?> removeUserFromCommunity(
            Principal principal, @PathVariable Long memberId) throws Exception {

        memberService.removeUserFromCommunity(principal, memberId);

        return ResponseEntity.ok(Map.of("message", "success", "data", "Current member has been removed from community"));
    }

    @PostMapping(
            value = "/photo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ProfilePhotoResponse> uploadProfilePhoto(
            Principal principal,
            @RequestParam("photo") MultipartFile photo
    ) {
        String photoUrl = imageUploadService.uploadProfilePhoto(principal, photo);

        return ResponseEntity.ok(
                new ProfilePhotoResponse(photoUrl)
        );
    }

    @GetMapping("/my-profile")
    public ResponseEntity<MemberProfileResponse> getMyProfile(Principal principal) {
        return ResponseEntity.ok(memberService.getCurrentMemberProfile(principal));
    }

    @PutMapping("/basic-info")
    public ResponseEntity<Void> updateBasicInfo(Principal principal, @RequestBody BasicInfoDTO dto) {
        memberService.updateBasicInfo(principal, dto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/father")
    public ResponseEntity<Void> updateFatherDetails(Principal principal, @RequestBody FatherDetailsDTO dto) {
        memberService.updateFatherDetails(principal, dto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/mother")
    public ResponseEntity<Void> updateMotherDetails(Principal principal, @RequestBody MotherDetailsDTO dto) {
        memberService.updateMotherDetails(principal, dto);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/spouse")
    public ResponseEntity<Void> updateSpouseDetails(Principal principal, @RequestBody SpouseDetailsDTO dto) {
        memberService.updateSpouseDetails(principal, dto);
        return ResponseEntity.noContent().build();
    }
}
