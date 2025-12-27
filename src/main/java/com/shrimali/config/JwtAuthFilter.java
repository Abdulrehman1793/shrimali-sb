package com.shrimali.config;

import com.shrimali.modules.auth.service.security.CustomUserDetailsService;
import com.shrimali.modules.auth.service.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/forgot-password" // add others as needed
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) return true;
        if (path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/")) return true;
        return PUBLIC_PATHS.contains(path);
    }

    private static final String COOKIE_NAME = "AUTH_TOKEN";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        String path = request.getRequestURI();
        // Skip OAuth endpoints so the OAuth2 filters can process the callback
        if (path.startsWith("/oauth2/") || path.startsWith("/login/oauth2/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);
        String username = null;

        if (token == null) {
            // Helpful debug logs to understand why token is null
            log.debug("JwtAuthFilter: no token found for request {} {}", request.getMethod(), path);

            log.debug(" Authorization header: {}", authHeader);
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                log.debug(" Request cookies: {}", Arrays.toString(cookies));
            } else {
                log.debug(" No cookies present on request");
            }
            // continue filter chain (will result in 401 at controller if endpoint requires auth)
            filterChain.doFilter(request, response);
            return;
        }

        try {
            username = jwtUtil.extractUsername(token);
        } catch (Exception e) {
            log.warn("Cannot extract username from token: {}", e.getMessage());
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1) Authorization header
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        // 2) Cookie fallback
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (COOKIE_NAME.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }

        return null;
    }
}
