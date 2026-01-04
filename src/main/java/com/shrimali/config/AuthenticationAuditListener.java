package com.shrimali.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationAuditListener {

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        String username = auth.getName();
        String ip = "unknown";

        if (auth.getDetails() instanceof WebAuthenticationDetails details) {
            ip = details.getRemoteAddress();
        }

        // Fixed log: clear, structured, and searchable
        log.info("AUTH_SUCCESS | User: {} | IP: {} | Provider: {}",
                username, ip, auth.getClass().getSimpleName());

        // Optional: Save to DB for the "Suspicious Activity" tracking we discussed
        // auditService.logLogin(username, "SUCCESS", ip);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        Authentication auth = event.getAuthentication();
        String username = (auth != null) ? auth.getName() : "anonymous";
        String error = event.getException().getMessage();
        String ip = "unknown";

        if (auth != null && auth.getDetails() instanceof WebAuthenticationDetails details) {
            ip = details.getRemoteAddress();
        }

        // Fixed log for failures
        log.warn("AUTH_FAILURE | User: {} | IP: {} | Reason: {}",
                username, ip, error);

        // auditService.logLogin(username, "FAILURE", ip, error);
    }
}