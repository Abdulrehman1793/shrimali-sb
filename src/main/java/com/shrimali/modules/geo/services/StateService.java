package com.shrimali.modules.geo.services;

import com.shrimali.model.geo.State;
import com.shrimali.repositories.geo.StateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StateService {
    private final StateRepository repository;

    public List<State> getStates(Long countryId, String q) {
        if (!StringUtils.hasText(q)) {
            q = null;
        }
        return repository.searchByCountry(countryId, q);
    }
}
