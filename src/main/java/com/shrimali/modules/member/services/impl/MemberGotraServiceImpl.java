package com.shrimali.modules.member.services.impl;

import com.shrimali.model.Gotra;
import com.shrimali.model.auth.User;
import com.shrimali.model.member.Member;
import com.shrimali.model.member.MemberGotra;
import com.shrimali.modules.member.dto.GotraDTO;
import com.shrimali.modules.member.dto.MemberGotraDTO;
import com.shrimali.modules.member.services.MemberGotraService;
import com.shrimali.repositories.GotraRepository;
import com.shrimali.repositories.MemberGotraRepository;
import com.shrimali.repositories.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberGotraServiceImpl implements MemberGotraService {

    private final MemberGotraRepository memberGotraRepository;
    private final MemberRepository memberRepository;
    private final GotraRepository gotraRepository;

    @Override
    public void setPrimaryGotra(Principal principal, Long gotraId) {
        Member member = getMember(principal);

        List<MemberGotra> gotras =
                memberGotraRepository.findByMemberId(member.getId());

        MemberGotra target = gotras.stream()
                .filter(mg -> mg.getGotra().getId().equals(gotraId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Gotra not assigned to member"));

        // unset all
        gotras.forEach(mg -> mg.setIsPrimary(Boolean.FALSE));

        // set selected
        target.setIsPrimary(Boolean.TRUE);

        memberGotraRepository.saveAll(gotras);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberGotraDTO getPrimaryGotra(Principal principal) {
        Member member = getMember(principal);

        return memberGotraRepository
                .findByMemberIdAndIsPrimaryTrue(member.getId())
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    public List<MemberGotraDTO> removeMemberGotra(Principal principal, Long gotraId) {
        Member member = getMember(principal);

        MemberGotra mg = memberGotraRepository
                .findByMemberIdAndGotraId(member.getId(), gotraId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Gotra not linked to member"));

        memberGotraRepository.delete(mg);

        return findByMember(principal);
    }

    @Override
    public MemberGotraDTO addMemberGotra(Principal principal, GotraDTO dto,boolean marriageCase) {
        Member member = getMember(principal);

        Gotra gotra = gotraRepository.findById(dto.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Gotra not found"));

        // Prevent duplicate
        boolean alreadyExists =
                memberGotraRepository.existsByMemberIdAndGotraId(
                        member.getId(), gotra.getId());

        if (alreadyExists) {
            throw new IllegalStateException("Gotra already assigned");
        }

        Optional<MemberGotra> currentPrimary =
                memberGotraRepository.findByMemberIdAndIsPrimaryTrue(member.getId());

        if (currentPrimary.isPresent()) {
            MemberGotra primary = currentPrimary.get();

            if (marriageCase) {
                // Marriage case → keep old gotra but make it non-primary
                primary.setIsPrimary(false);
                memberGotraRepository.save(primary);
            } else {
                // Normal case → delete old gotra completely
                memberGotraRepository.delete(primary);
            }
        }

        // New gotra is ALWAYS primary
        MemberGotra newGotra = MemberGotra.builder()
                .member(member)
                .gotra(gotra)
                .isPrimary(true)
                .build();

        memberGotraRepository.save(newGotra);

        return toDto(newGotra);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberGotraDTO> findByMember(Principal principal) {
        Member member = getMember(principal);

        return memberGotraRepository
                .findByMemberIdOrdered(member.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

     /* -------------------------------------------------
       HELPERS
       ------------------------------------------------- */

    private Member getMember(Principal principal) {
        User userPrincipal = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        assert userPrincipal != null;
        Long memberId = userPrincipal.getMemberId();

        return memberRepository.findById(memberId)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Member not found")
                );
    }

    private MemberGotraDTO toDto(Gotra g) {
        return MemberGotraDTO.builder()
                .id(g.getId())
                .name(g.getName())
                .description(g.getDescription())
                .build();
    }

    private MemberGotraDTO toDto(MemberGotra mg) {
        Gotra g = mg.getGotra();

        return MemberGotraDTO.builder()
                .id(mg.getId())
                .gotraId(g.getId())
                .name(g.getName())
                .description(g.getDescription())
                .isPrimary(Boolean.TRUE.equals(mg.getIsPrimary()))
                .build();
    }
}
