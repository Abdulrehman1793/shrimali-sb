package com.shrimali.modules.member.controller;

import com.shrimali.modules.member.dto.ContactPayload;
import com.shrimali.modules.member.services.MemberContactService;
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
@RequestMapping("/api/v1/members/contact")
@RequiredArgsConstructor
@Slf4j
@Validated
public class MemberContactController {
    private final MemberContactService memberContactService;

    /* -------------------- LIST CONTACTS -------------------- */
    @GetMapping
    public ResponseEntity<List<ContactPayload>> listContacts(Principal principal) {
        log.debug("Fetching member contacts");
        List<ContactPayload> contacts = memberContactService.listContacts(principal);
        return ResponseEntity.ok(contacts);
    }

    /* -------------------- ADD CONTACT -------------------- */
    @PostMapping
    public ResponseEntity<?> addContact(
            Principal principal,
            @Valid @RequestBody ContactPayload payload
    ) {
        log.debug("Adding contact: {}", payload);
        memberContactService.addContact(principal, payload);
        return ResponseEntity.ok(
                Map.of("message", "Contact added successfully")
        );
    }

    /* -------------------- UPDATE CONTACT -------------------- */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactPayload payload
    ) {
        log.debug("Updating contact: {}", payload);
        memberContactService.updateContact(id, payload);
        return ResponseEntity.ok(Map.of("message", "Contact updated successfully"));
    }

    /* -------------------- DELETE CONTACT -------------------- */
    @DeleteMapping
    public ResponseEntity<?> removeContact(
            Principal principal,
            @Valid @RequestBody ContactPayload payload
    ) {
        log.debug("Removing contact: {}", payload);
        memberContactService.removeContact(principal, payload);
        return ResponseEntity.ok(
                Map.of("message", "Contact removed successfully")
        );
    }
}
