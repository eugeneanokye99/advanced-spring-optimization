package com.shopjoy.service;

import com.shopjoy.entity.SecurityAuditLog;
import com.shopjoy.entity.SecurityEventType;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for managing security audit logs.
 * Handles logging of security events and querying audit trails.
 */
public interface SecurityAuditService {

    /**
     * Log a security event asynchronously.
     *
     * @param username  the username (can be null for anonymous events)
     * @param eventType the type of security event
     * @param request   the HTTP request (used to extract IP and user agent)
     * @param details   additional details about the event
     * @param success   whether the event was successful
     */
    void logEvent(String username, SecurityEventType eventType, HttpServletRequest request, String details, Boolean success);

    /**
     * Log a security event without HTTP request context.
     *
     * @param username  the username
     * @param eventType the type of security event
     * @param details   additional details about the event
     * @param success   whether the event was successful
     */
    void logEvent(String username, SecurityEventType eventType, String details, Boolean success);

    /**
     * Get all audit logs with pagination.
     *
     * @param pageable pagination information
     * @return page of audit logs
     */
    Page<SecurityAuditLog> getAllLogs(Pageable pageable);

    /**
     * Get audit logs by username.
     *
     * @param username the username
     * @param pageable pagination information
     * @return page of audit logs
     */
    Page<SecurityAuditLog> getLogsByUsername(String username, Pageable pageable);

    /**
     * Get audit logs by event type.
     *
     * @param eventType the event type
     * @param pageable  pagination information
     * @return page of audit logs
     */
    Page<SecurityAuditLog> getLogsByEventType(SecurityEventType eventType, Pageable pageable);

    /**
     * Get audit logs within a date range.
     *
     * @param startTime start timestamp
     * @param endTime   end timestamp
     * @param pageable  pagination information
     * @return page of audit logs
     */
    Page<SecurityAuditLog> getLogsByDateRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * Get audit logs by username and event type.
     *
     * @param username  the username
     * @param eventType the event type
     * @param pageable  pagination information
     * @return page of audit logs
     */
    Page<SecurityAuditLog> getLogsByUsernameAndEventType(String username, SecurityEventType eventType, Pageable pageable);

    /**
     * Get recent failed login attempts for a username.
     *
     * @param username the username
     * @param minutes  number of minutes to look back
     * @return list of failed login attempts
     */
    List<SecurityAuditLog> getRecentFailedLogins(String username, int minutes);

    /**
     * Count security events by type within a time range.
     *
     * @param eventType the event type
     * @param startTime start timestamp
     * @param endTime   end timestamp
     * @return count of events
     */
    Long countEventsByType(SecurityEventType eventType, LocalDateTime startTime, LocalDateTime endTime);
}
