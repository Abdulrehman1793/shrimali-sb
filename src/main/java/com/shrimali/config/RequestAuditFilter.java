package com.shrimali.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class RequestAuditFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String ip = request.getRemoteAddr();
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // 1. Process the request
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            // 2. Identify the user (if logged in)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = (auth != null && auth.isAuthenticated()) ? auth.getName() : "ANONYMOUS";

            // 3. Log the activity
            // Format: [IP] [USER] [METHOD] [URI] [STATUS] [DURATION ms]
            log.info("REQ_AUDIT | IP: {} | User: {} | {} {} | Status: {} | Time: {}ms",
                    ip, username, method, uri, status, duration);

            // 4. Security Logic Hook (Example: Basic Rate Limiting check)
            // if (isIpBlacklisted(ip)) { ... throw SecurityException ... }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // Skip noisy static assets to keep logs clean
        return path.startsWith("/assets/") || path.endsWith(".ico") || path.endsWith(".js");
    }
}
