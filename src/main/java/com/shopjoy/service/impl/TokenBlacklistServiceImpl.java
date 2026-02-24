package com.shopjoy.service.impl;

import com.shopjoy.service.TokenBlacklistService;
import com.shopjoy.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token blacklist service with ConcurrentHashMap and scheduled cleanup.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final JwtUtil jwtUtil;
    
    private final Map<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();
    
    private static final int MAX_BLACKLIST_SIZE = 10000;

    @Override
    public void blacklistToken(String token) {
        try {
            if (blacklistedTokens.size() >= MAX_BLACKLIST_SIZE) {
                log.warn("Blacklist approaching maximum size ({}). Running cleanup...", MAX_BLACKLIST_SIZE);
                removeExpiredTokens();
            }
            
            Date expiration = jwtUtil.extractExpiration(token);
            LocalDateTime expirationTime = expiration.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            
            blacklistedTokens.put(token, expirationTime);
            
            log.debug("Token blacklisted. Total blacklisted tokens: {}", blacklistedTokens.size());
            
        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", e.getMessage());
            blacklistedTokens.put(token, LocalDateTime.now().plusDays(1));
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    /**
     * Removes expired tokens. Runs hourly.
     */
    @Scheduled(fixedRate = 3600000)
    @Override
    public void removeExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int initialSize = blacklistedTokens.size();
        
        blacklistedTokens.entrySet().removeIf(entry -> {
            boolean isExpired = entry.getValue().isBefore(now);
            if (isExpired) {
                log.trace("Removing expired token from blacklist");
            }
            return isExpired;
        });
        
        int removedCount = initialSize - blacklistedTokens.size();
        
        if (removedCount > 0) {
            log.info("Blacklist cleanup: Removed {} expired tokens. Current size: {}", 
                    removedCount, blacklistedTokens.size());
        } else {
            log.debug("Blacklist cleanup: No expired tokens found. Current size: {}", 
                    blacklistedTokens.size());
        }
    }
}
