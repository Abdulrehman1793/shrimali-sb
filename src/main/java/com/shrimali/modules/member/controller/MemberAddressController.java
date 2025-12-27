package com.shrimali.modules.member.controller;

import com.shrimali.modules.member.dto.MemberAddressPayload;
import com.shrimali.modules.member.services.MemberAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/members/addresses")
@RequiredArgsConstructor
@Slf4j
@Validated
public class MemberAddressController {

    private final MemberAddressService memberAddressService;

    /* -------------------- LIST ADDRESSES -------------------- */
    @GetMapping
    public ResponseEntity<List<MemberAddressPayload>> listAddresses(
            Principal principal
    ) {
        log.debug("Fetching member addresses");
        return ResponseEntity.ok(
                memberAddressService.list(principal)
        );
    }

    /* -------------------- ADD ADDRESS -------------------- */
    @PostMapping
    public ResponseEntity<?> addAddress(
            Principal principal,
            @Valid @RequestBody MemberAddressPayload payload
    ) {
        log.debug("Adding address: {}", payload);
        memberAddressService.add(principal, payload);
        return ResponseEntity.ok(
                Map.of("message", "Address added successfully")
        );
    }

    /* -------------------- UPDATE ADDRESS -------------------- */
    @PutMapping
    public ResponseEntity<?> updateAddress(
            Principal principal,
            @Valid @RequestBody MemberAddressPayload payload
    ) {
        log.debug("Updating address: {}", payload);
        memberAddressService.update(principal, payload);
        return ResponseEntity.ok(
                Map.of("message", "Address updated successfully")
        );
    }

    /* -------------------- DELETE ADDRESS -------------------- */
    @DeleteMapping("/{addressType}")
    public ResponseEntity<?> deleteAddress(
            Principal principal,
            @PathVariable String addressType
    ) {
        log.debug("Deleting address of type: {}", addressType);
        memberAddressService.remove(principal, addressType);
        return ResponseEntity.ok(
                Map.of("message", "Address deleted successfully")
        );
    }
}

