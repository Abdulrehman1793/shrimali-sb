package com.shrimali.modules.geo.services;

import com.shrimali.dto.PagedResponse;
import com.shrimali.model.geo.Country;
import com.shrimali.modules.geo.dto.CountryDTO;
import com.shrimali.repositories.geo.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryService {
    private final CountryRepository repository;

    public PagedResponse<CountryDTO> getCountries(String q, PageRequest pageRequest) {
        Page<Country> page;
        if (StringUtils.hasText(q)) {
            page = repository.search(q.trim(), pageRequest);
        } else {
            page = repository.findAllByOrderByNameAsc(pageRequest);
        }

        return new PagedResponse<>(
                toDTO(page.toList()),
                PageRequest.of(
                        page.getPageable().getPageNumber(),
                        page.getPageable().getPageSize(),
                        page.getSort()),
                page.getTotalElements()
        );
    }

    List<CountryDTO> toDTO(List<Country> countries) {
        return countries.stream()
                .map(country ->
                        new CountryDTO(country.getId(), country.getCode(), country.getName()))
                .toList();
    }
}
