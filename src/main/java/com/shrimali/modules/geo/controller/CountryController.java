package com.shrimali.modules.geo.controller;

import com.shrimali.dto.PagedResponse;
import com.shrimali.modules.geo.dto.CountryDTO;
import com.shrimali.modules.geo.services.CountryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/countries")
public class CountryController {
    private final CountryService countryService;

    @GetMapping
    public PagedResponse<CountryDTO> getCountries(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {

        return countryService.getCountries(q,
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name")));
    }
}
