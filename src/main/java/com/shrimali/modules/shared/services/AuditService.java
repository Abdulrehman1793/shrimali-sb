package com.shrimali.modules.shared.services;

import com.shrimali.model.auditing.AuditLog;
import com.shrimali.repositories.AuditLogRepository;
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

    /**
     * General purpose logging for actions like Registration or Profile Updates.
     */
    public void logAction(String targetEmail, String action, String details) {
        String actorEmail = getCurrentUserEmail();

        // If it's a registration, the actor and target are the same person
        if (actorEmail.equals("anonymousUser")) {
            actorEmail = targetEmail;
        }

        saveLog(actorEmail, targetEmail, action, details);
    }

    /**
     * Specific logging for Authority/Role changes.
     */
    public void logRoleChange(String targetEmail, String roleName, boolean added) {
        String actorEmail = getCurrentUserEmail();
        String action = added ? "ROLE_ADDED" : "ROLE_REMOVED";
        String details = String.format("Role [%s] was %s by %s",
                roleName, added ? "assigned" : "revoked", actorEmail);

        saveLog(actorEmail, targetEmail, action, details);
    }

    private void saveLog(String actor, String target, String action, String details) {
        AuditLog auditLog = AuditLog.builder()
                .actor(actor)
                .target(target)
                .action(action)
                .details(details)
                .ipAddress(getClientIp())
                .timestamp(OffsetDateTime.now())
                .build();

        auditLogRepository.save(auditLog);
        log.info("AUDIT | Action: {} | Actor: {} | Target: {}", action, actor, target);
    }

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "anonymousUser";
        }
        return auth.getName();
    }

    private String getClientIp() {
        String remoteAddr = request.getHeader("X-FORWARDED-FOR");
        if (remoteAddr == null || remoteAddr.isEmpty()) {
            remoteAddr = request.getRemoteAddr();
        }
        return remoteAddr;
    }
}
