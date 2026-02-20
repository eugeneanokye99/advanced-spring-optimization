package com.shopjoy.service.impl;

import com.shopjoy.entity.SecurityAuditLog;
import com.shopjoy.entity.SecurityEventType;
import com.shopjoy.repository.SecurityAuditLogRepository;
import com.shopjoy.service.SecurityAuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of SecurityAuditService for managing security audit logs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAuditServiceImpl implements SecurityAuditService {

    private final SecurityAuditLogRepository auditLogRepository;

    @Async
    @Transactional
    @Override
    public void logEvent(String username, SecurityEventType eventType, HttpServletRequest request, String details, Boolean success) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .username(username)
                    .eventType(eventType)
                    .ipAddress(extractClientIp(request))
                    .userAgent(extractUserAgent(request))
                    .details(details)
                    .success(success)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Logged security event: {} for user: {}", eventType, username);
        } catch (Exception e) {
            log.error("Failed to log security event: {} for user: {}", eventType, username, e);
        }
    }

    @Async
    @Transactional
    @Override
    public void logEvent(String username, SecurityEventType eventType, String details, Boolean success) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .username(username)
                    .eventType(eventType)
                    .details(details)
                    .success(success)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Logged security event: {} for user: {}", eventType, username);
        } catch (Exception e) {
            log.error("Failed to log security event: {} for user: {}", eventType, username, e);
        }
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    private String extractUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String userAgent = request.getHeader("User-Agent");
        if (userAgent != null && userAgent.length() > 500) {
            return userAgent.substring(0, 500);
        }
        return userAgent;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<SecurityAuditLog> getAllLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<SecurityAuditLog> getLogsByUsername(String username, Pageable pageable) {
        return auditLogRepository.findByUsername(username, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<SecurityAuditLog> getLogsByEventType(SecurityEventType eventType, Pageable pageable) {
        return auditLogRepository.findByEventType(eventType, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<SecurityAuditLog> getLogsByDateRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return auditLogRepository.findByTimestampBetween(startTime, endTime, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<SecurityAuditLog> getLogsByUsernameAndEventType(String username, SecurityEventType eventType, Pageable pageable) {
        return auditLogRepository.findByUsernameAndEventType(username, eventType, pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SecurityAuditLog> getRecentFailedLogins(String username, int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        return auditLogRepository.findRecentFailedAttempts(username, SecurityEventType.LOGIN_FAILURE, since);
    }

    @Transactional(readOnly = true)
    @Override
    public Long countEventsByType(SecurityEventType eventType, LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.countByEventTypeAndTimestampBetween(eventType, startTime, endTime);
    }
}
