package com.shopjoy.config;

import com.shopjoy.entity.SecurityEventType;
import com.shopjoy.service.CustomUserDetailsService;
import com.shopjoy.service.SecurityAuditService;
import com.shopjoy.service.TokenBlacklistService;
import com.shopjoy.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityAuditService securityAuditService;
    private final TokenBlacklistService tokenBlacklistService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/api/v1/auth/",
        "/oauth2/",
        "/login/oauth2/",
        "/demo/"
    );

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();


        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String ipAddress = SecurityAuditService.extractClientIp(request);
        String userAgent = SecurityAuditService.extractUserAgent(request);

        String jwtToken = extractTokenFromRequest(request);
        
        if (jwtToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isTokenBlacklisted(jwtToken, ipAddress, userAgent)) {
            filterChain.doFilter(request, response);
            return;
        }

        authenticateUserFromToken(jwtToken, request, ipAddress, userAgent);
        
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }
        
        return authHeader.substring(BEARER_PREFIX.length());
    }

    private boolean isTokenBlacklisted(String token, String ipAddress, String userAgent) {
        if (tokenBlacklistService.isBlacklisted(token)) {
            log.debug("Token is blacklisted (user logged out)");
            securityAuditService.logEvent(
                null,
                SecurityEventType.ACCESS_DENIED,
                ipAddress,
                userAgent,
                "Attempted to use blacklisted token",
                false
            );
            return true;
        }
        return false;
    }

    private void authenticateUserFromToken(
            String token,
            HttpServletRequest request,
            String ipAddress,
            String userAgent
    ) {
        try {
            String username = jwtUtil.extractUsername(token);
            
            if (username == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                return;
            }

            if (jwtUtil.isTokenExpired(token)) {
                log.warn("JWT token expired for request: {}", request.getRequestURI());
                securityAuditService.logEvent(
                    username,
                    SecurityEventType.TOKEN_EXPIRED,
                    ipAddress,
                    userAgent,
                    "JWT token expired: " + request.getRequestURI(),
                    false
                );
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (isUsernameMatching(token, userDetails)) {
                setAuthentication(userDetails, request);
                log.debug("JWT authentication successful for user: {}", username);
            } else {
                logTokenValidationFailure(username, ipAddress, userAgent);
            }

        } catch (Exception e) {
            SecurityEventType eventType = e instanceof ExpiredJwtException 
                    ? SecurityEventType.TOKEN_EXPIRED 
                    : SecurityEventType.TOKEN_INVALID;
            
            String username = e instanceof ExpiredJwtException 
                    ? ((ExpiredJwtException) e).getClaims().getSubject() 
                    : null;
            
            log.warn("JWT authentication failed: {}", e.getMessage());
            securityAuditService.logEvent(
                username,
                eventType,
                ipAddress,
                userAgent,
                "JWT authentication error: " + e.getMessage(),
                false
            );
        }
    }

    private boolean isUsernameMatching(String token, UserDetails userDetails) {
        String username = jwtUtil.extractUsername(token);
        return username.equals(userDetails.getUsername());
    }

    private void setAuthentication(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private void logTokenValidationFailure(String username, String ipAddress, String userAgent) {
        log.warn("Invalid JWT token for user: {}", username);
        securityAuditService.logEvent(
            username,
            SecurityEventType.TOKEN_INVALID,
            ipAddress,
            userAgent,
            "Token validation failed",
            false
        );
    }
}
