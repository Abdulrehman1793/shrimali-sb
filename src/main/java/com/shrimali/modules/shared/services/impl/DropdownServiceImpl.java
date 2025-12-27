package com.shrimali.modules.shared.services.impl;

import com.shrimali.modules.shared.dto.DropdownDTO;
import com.shrimali.modules.shared.services.DropdownService;
import com.shrimali.repositories.GotraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DropdownServiceImpl implements DropdownService {
    private final GotraRepository gotraRepository;

    @Override
    public List<DropdownDTO> getAll() {
        return gotraRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .map(g -> DropdownDTO.builder()
                        .id(g.getId())
                        .name(g.getName())
                        .build())
                .toList();
    }
}
