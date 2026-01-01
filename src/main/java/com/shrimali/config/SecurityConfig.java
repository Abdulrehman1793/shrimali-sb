package com.shrimali.config;

import com.shrimali.modules.auth.service.security.CustomOAuth2SuccessHandler;
import com.shrimali.modules.auth.service.security.CustomOidcUserService;
import com.shrimali.modules.auth.service.security.CustomUserDetailsService;
import com.shrimali.modules.auth.service.security.HttpCookieOAuth2AuthorizationRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class SecurityConfig {
    private static final String[] API_WHITELIST = {
            "/api/v1/auth/**", "/api/v1/welcome/**",
            "/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/me", "/api/v1/auth/logout",
            "/api/v1/auth/reset-password", "/api/v1/auth/forgot-password",
            "/api/v1/members/all",
            "/actuator/**"
    };

    private static final String[] WEB_WHITELIST = {
            "/", "/login**", "/oauth2/**", "/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/me", "/api/v1/auth/logout", "/favicon.ico"
    };

    private final @Lazy JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService customUserDetailsService;

    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final CustomOidcUserService customOidcUserService;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    /**
     * API security chain: matches API paths, stateless, returns 401 for unauthenticated requests,
     * uses JWT filter and DOES NOT configure oauth2Login (so no browser redirect).
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**", "/sb/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req -> req
                        .requestMatchers(API_WHITELIST).permitAll()
                        .anyRequest().authenticated()
                )
                // ensure API returns 401 (no redirect to login page)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )
                // add JWT filter for API requests
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Web (UI) security chain: default, stateful, includes oauth2Login flow for browser users.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(
            HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(req -> req
                        .requestMatchers(WEB_WHITELIST).permitAll()
                        // If you want some UI endpoints to require authentication explicitly, add them here.
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService)
                        )
                        .successHandler(customOAuth2SuccessHandler)
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return customUserDetailsService;
    }

    //    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
}
