package com.shrimali.modules.shared.controller;

import com.shrimali.modules.shared.dto.DropdownDTO;
import com.shrimali.modules.shared.services.DropdownService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dropdowns")
@RequiredArgsConstructor
public class DropdownController {
    private final DropdownService dropdownService;

    /**
     * Fetch all gotras for dropdown usage
     */
    @GetMapping("/gotras")
    public List<DropdownDTO> list() {
        return dropdownService.getAll();
    }
}
