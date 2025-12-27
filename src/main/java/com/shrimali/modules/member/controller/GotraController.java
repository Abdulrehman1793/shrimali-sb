package com.shrimali.modules.member.controller;

import com.shrimali.modules.member.dto.GotraDTO;
import com.shrimali.modules.member.services.GotraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/gotras")
@RequiredArgsConstructor
public class GotraController {
    private final GotraService gotraService;

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<List<GotraDTO>> list(Principal principal) {
        return ResponseEntity.ok(gotraService.list(principal));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<Void> add(Principal principal, @RequestBody @Valid GotraDTO gotraDTO) {
        gotraService.add(principal, gotraDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(Principal principal, @PathVariable Long id, @RequestBody @Valid GotraDTO gotraDTO) {
        gotraDTO.setId(id);
        gotraService.update(principal, gotraDTO);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(Principal principal, @PathVariable Long id) {
        gotraService.remove(principal, GotraDTO.builder().id(id).build());
        return ResponseEntity.noContent().build();
    }
}
