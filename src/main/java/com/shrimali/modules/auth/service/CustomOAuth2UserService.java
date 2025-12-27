package com.shrimali.modules.auth.service;

import com.shrimali.model.auth.User;
import com.shrimali.model.enums.AuthProviderType;
import com.shrimali.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
//    private final RoleService roleService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Fetch user information using the default service
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        // 2. Extract provider and email
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String email = oauth2User.getAttribute("email"); // Email is typically the primary identifier

        // 3. Process the user: Find or Create
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Check if the user's current provider is LOCAL and update if necessary, or just update last login
            if (!registrationId.equalsIgnoreCase(user.getAuthProvider().name())) {
                // Handle linking accounts if needed, e.g., if a user previously logged in with a password
                // For simplicity, we just update authProvider if it's null or different.
                user.setAuthProvider(AuthProviderType.GOOGLE);
            }
            user.setLastLoginAt(OffsetDateTime.now());
            // Optional: Update name, picture, etc. if provided by Google
        } else {
            // New user registration
            user = registerNewUser(registrationId, email, oauth2User.getAttribute("name"));
        }

        userRepository.save(user);

        // 4. Wrap the persistent User object to return for Spring Security
        // You might need a custom implementation of OAuth2User to bridge your User object
        // with the security principal, but for simple use cases, you can use the original
        // OAuth2User or create a simple wrapper.

        return oauth2User; // Or a custom principal
    }

    private User registerNewUser(String registrationId, String email, String name) {
        // Implement logic to create a new User object from Google profile data
        User newUser = User.builder()
                .email(email)
                // Password should be null or a unique placeholder for OAuth users
                .passwordHash(null)
                .status("ACTIVE")
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .lastLoginAt(OffsetDateTime.now())
                .authProvider(AuthProviderType.GOOGLE)
                .build();

        // Assign a default role, e.g., "ROLE_USER"
        // Role defaultRole = roleService.findByName("ROLE_USER");
        // newUser.setUserRoles(Set.of(new UserRole(null, newUser, defaultRole, OffsetDateTime.now())));

        return newUser;
    }
}
