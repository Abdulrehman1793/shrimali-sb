package com.shrimali.modules.shared.services;

import com.shrimali.model.auditing.AuditLog;
import com.shrimali.model.auth.User;
import com.shrimali.repositories.AuditLogRepository;
import com.shrimali.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest request;

    private final UserRepository userRepository;

    /**
     * Logs Family Tree changes (Adding children, updating deceased profiles).
     */
    public void logMemberAction(String action, Long targetMemberId, String details) {
        User actor = getCurrentUserEntity();
        saveLog(actor, action, "MEMBER_ID: " + targetMemberId + " | " + details);
    }

    /**
     * Logs Claim Transitions (The Token-sharing process).
     */
    public void logClaimAction(String action, String requesterEmail, String details) {
        User actor = getCurrentUserEntity();
        saveLog(actor, action, "Requester: " + requesterEmail + " | " + details);
    }

    /**
     * Legacy support for general actions.
     */
    public void logAction(String action, String details) {
        saveLog(getCurrentUserEntity(), action, details);
    }

    private void saveLog(User actor, String action, String details) {
        AuditLog auditLog = AuditLog.builder()
                .actor(actor) // Now stores the actual User object
                .action(action)
                .details(details)
                .ipAddress(getClientIp())
                .timestamp(OffsetDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.info("AUDIT | Action: {} | Actor: {} | Details: {}",
                action, (actor != null ? actor.getEmail() : "SYSTEM"), details);
    }

    private User getCurrentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null; // System action or Registration
        }
        // Fetch the actual User entity from DB to maintain the Foreign Key relationship
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    private String getClientIp() {
        String xf = request.getHeader("X-FORWARDED-FOR");
        if (xf != null && !xf.isEmpty()) {
            return xf.split(",")[0]; // Get the first IP in the chain
        }
        return request.getRemoteAddr();
    }
}