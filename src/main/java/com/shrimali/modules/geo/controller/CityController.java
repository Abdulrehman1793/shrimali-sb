package com.shrimali.modules.geo.controller;

import com.shrimali.model.geo.City;
import com.shrimali.modules.geo.services.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/cities")
public class CityController {
    private final CityService cityService;

    @GetMapping
    public List<City> getCities(@RequestParam Long stateId, @RequestParam(required = false) String q) {
        return cityService.getCities(stateId, q);
    }
}
