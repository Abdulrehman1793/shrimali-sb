package com.shrimali.modules;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/welcome")
@RequiredArgsConstructor
@Slf4j
@Validated
public class WelcomeController {
    @GetMapping
    public String welcome() {
        return "Welcome to Shrimali!";
    }
}
