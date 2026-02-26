package com.shopjoy.service.impl;

import com.shopjoy.entity.SecurityAuditLog;
import com.shopjoy.entity.SecurityEventType;
import com.shopjoy.repository.SecurityAuditLogRepository;
import com.shopjoy.service.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityAuditServiceImpl implements SecurityAuditService {

    private final SecurityAuditLogRepository auditLogRepository;

    @Async("taskExecutor")
    @Transactional
    @Override
    public void logEvent(String username, SecurityEventType eventType, String ipAddress, String userAgent, String details, Boolean success) {
        try {
            SecurityAuditLog auditLog = SecurityAuditLog.builder()
                    .username(username)
                    .eventType(eventType)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .details(details)
                    .success(success)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Logged security event: {} for user: {}", eventType, username);
        } catch (Exception e) {
            log.error("Failed to log security event: {} for user: {}", eventType, username, e);
        }
    }

    @Async("taskExecutor")
    @Transactional
    @Override
    public void logEvent(String username, SecurityEventType eventType, String details, Boolean success) {
        logEvent(username, eventType, null, null, details, success);
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
