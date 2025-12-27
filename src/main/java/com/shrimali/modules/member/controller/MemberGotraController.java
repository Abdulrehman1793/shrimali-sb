package com.shrimali.modules.member.controller;

import com.shrimali.modules.member.dto.GotraDTO;
import com.shrimali.modules.member.dto.MemberGotraDTO;
import com.shrimali.modules.member.services.MemberGotraService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/member/gotras")
@RequiredArgsConstructor
public class MemberGotraController {
    private final MemberGotraService memberGotraService;

    @GetMapping
    public List<MemberGotraDTO> listMyGotras(Principal principal) {
        return memberGotraService.findByMember(principal);
    }

    @PostMapping
    public MemberGotraDTO addGotra(Principal principal, @RequestBody @Valid GotraDTO gotraDTO) {
        return memberGotraService.addMemberGotra(principal, gotraDTO, false);
    }

    @DeleteMapping("/{gotraId}")
    public List<MemberGotraDTO> removeGotra(Principal principal, @PathVariable Long gotraId) {
        return memberGotraService.removeMemberGotra(principal, gotraId);
    }

    @GetMapping("/primary")
    public MemberGotraDTO getPrimaryGotra(Principal principal) {
        return memberGotraService.getPrimaryGotra(principal);
    }

    @PutMapping("/{gotraId}/primary")
    public void setPrimaryGotra(Principal principal, @PathVariable Long gotraId) {
        memberGotraService.setPrimaryGotra(principal, gotraId);
    }
}
