package com.shrimali.modules.member.services.impl;

import com.shrimali.model.Gotra;
import com.shrimali.model.auth.User;
import com.shrimali.modules.member.dto.GotraDTO;
import com.shrimali.modules.member.services.GotraService;
import com.shrimali.repositories.GotraRepository;
import com.shrimali.repositories.MemberGotraRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GotraServiceImpl implements GotraService {
    private final GotraRepository gotraRepository;
    private final MemberGotraRepository memberGotraRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GotraDTO> list(Principal principal) {
        return gotraRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public void add(Principal principal, GotraDTO gotraDTO) {
        User user = getUser(principal);

        if (gotraRepository.existsByNameIgnoreCase(gotraDTO.getName())) {
            throw new IllegalArgumentException("Gotra already exists");
        }

        Gotra gotra = Gotra.builder()
                .name(gotraDTO.getName().trim())
                .description(gotraDTO.getDescription())
                .createdBy(user)
                .build();

        gotraRepository.save(gotra);
    }

    @Override
    public void update(Principal principal, GotraDTO gotraDTO) {
        User user = getUser(principal);

        Gotra gotra = gotraRepository.findById(gotraDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Gotra not found"));

        // prevent duplicate name
        gotraRepository.findByNameIgnoreCase(gotraDTO.getName())
                .filter(g -> !g.getId().equals(gotra.getId()))
                .ifPresent(g -> {
                    throw new IllegalArgumentException("Gotra name already in use");
                });

//        gotra.setName(gotraDTO.getName().trim());
        gotra.setDescription(gotraDTO.getDescription());
        gotra.setUpdatedBy(user);

        gotraRepository.save(gotra);
    }

    @Override
    public void remove(Principal principal, GotraDTO gotraDTO) {
        Long gotraId = gotraDTO.getId();

        if (memberGotraRepository.existsByGotraId(gotraId)) {
            throw new IllegalStateException(
                    "Cannot delete gotra. It is already assigned to members."
            );
        }

        Gotra gotra = gotraRepository.findById(gotraDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Gotra not found"));

        gotraRepository.delete(gotra);
    }

    private User getUser(Principal principal) {
        return (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
    }

    private GotraDTO toDTO(Gotra gotra) {
        return GotraDTO.builder()
                .id(gotra.getId())
                .name(gotra.getName())
                .description(gotra.getDescription())
                .build();
    }
}
