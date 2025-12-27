package com.shrimali.modules.geo.services;

import com.shrimali.model.geo.City;
import com.shrimali.repositories.geo.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {
    private final CityRepository repository;

    public List<City> getCities(Long stateId, String q) {
        if (!StringUtils.hasText(q)) {
            q = null;
        }
        return repository.searchByState(stateId, q);
    }
}
