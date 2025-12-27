package com.shrimali.modules.auth.service.security;

import com.shrimali.model.auth.User;
import com.shrimali.model.enums.UserStatus;
import com.shrimali.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtTokenService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final Environment environment;

    private String getClientRedirectUrl() {
        return environment.getProperty("app.client.url", "https://shrimalis.com");
    }

    private String getCookieDomain() {
        // optional: blank => don't set Domain (safer for single-domain)
        return environment.getProperty("app.cookie.domain", "");
    }

    private static final String CLIENT_REDIRECT_URL = "http://localhost:5173"; // SPA origin
    private static final String COOKIE_NAME = "AUTH_TOKEN";
    private static final int COOKIE_MAX_AGE = 7 * 24 * 60 * 60;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        if (email == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email required");
            return;
        }

        // create or find user
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setStatus(UserStatus.ACTIVE);
            // set other fields if needed
            return userRepository.save(u);
        });

        String jwt = jwtTokenService.generateToken(user.getEmail());

        // Set cookie attributes
        // "AUTH_TOKEN"
        // seconds
        String cookieDomain = getCookieDomain(); // may be empty -> omit Domain
        boolean secure = true; // true in production (https)
        boolean httpOnly = true;

        // Build cookie string with SameSite attribute (HttpServlet Cookie API doesn't support SameSite directly)
        StringBuilder sb = new StringBuilder();
        sb.append(COOKIE_NAME).append("=").append(jwt)
                .append("; Max-Age=").append(COOKIE_MAX_AGE)
                .append("; Path=/");
        if (!cookieDomain.isBlank()) {
            sb.append("; Domain=").append(cookieDomain);
        }
        sb.append("; Secure");
        sb.append("; HttpOnly");
        sb.append("; SameSite=Lax");

        response.addHeader("Set-Cookie", sb.toString());

        // Redirect to SPA
        String target = getClientRedirectUrl() + "/oauth2/redirect";
        getRedirectStrategy().sendRedirect(request, response, target);
    }
}
