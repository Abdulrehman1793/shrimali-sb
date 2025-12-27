package com.shrimali.modules.geo.controller;

import com.shrimali.model.geo.State;
import com.shrimali.modules.geo.services.StateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/states")
public class StateController {
    private final StateService stateService;

    @GetMapping
    public List<State> getStates(@RequestParam Long countryId, @RequestParam(required = false) String q) {
        return stateService.getStates(countryId, q);
    }
}
