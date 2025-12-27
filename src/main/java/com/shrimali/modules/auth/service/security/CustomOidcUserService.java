package com.shrimali.modules.auth.service.security;

import com.shrimali.model.enums.AuthProviderType;
import com.shrimali.modules.auth.dto.RegistrationDto;
import com.shrimali.modules.auth.service.UserRegistrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRegistrationService userRegistrationService;

    @Transactional
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        Map<String, Object> attrs = oidcUser.getAttributes();

        RegistrationDto dto = RegistrationDto.builder()
                .email(oidcUser.getEmail())
                .firstName((String) attrs.get("given_name"))
                .lastName((String) attrs.get("family_name"))
                .photoUrl((String) attrs.get("picture"))
                .providerId(oidcUser.getSubject()) // Google's unique ID
                .authProvider(AuthProviderType.GOOGLE)
                .emailVerified(true)
                .build();

        userRegistrationService.registerOrUpdateUser(dto);
        return oidcUser;
    }
}
