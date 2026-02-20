package com.shopjoy.security;

import com.shopjoy.entity.SecurityEventType;
import com.shopjoy.entity.User;
import com.shopjoy.entity.UserType;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.service.SecurityAuditService;
import com.shopjoy.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * OAuth2 login success handler with JWT token generation.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final SecurityAuditService securityAuditService;


    /**
     * Handles OAuth2 authentication success.
     */
    @Override
    @Transactional
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        log.info("OAuth2 authentication successful for user: {}", authentication.getName());
        
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            
            assert oAuth2User != null;
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            String givenName = (String) attributes.get("given_name");
            String familyName = (String) attributes.get("family_name");
            String providerId = (String) attributes.get("sub");
            String provider = determineProvider(request);
            
            log.info("OAuth2 user details - Email: {}, Name: {}, Provider: {}", email, name, provider);
            
            if (email == null || email.isEmpty()) {
                log.error("OAuth2 authentication failed: Email not provided by OAuth2 provider");
                redirectToFrontendWithError(response, "email_not_provided");
                return;
            }
            
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createNewOAuth2User(email, givenName, familyName, name, provider, providerId));
            
            if (user.getOauthProvider() == null) {
                user.setOauthProvider(provider);
                user.setOauthProviderId(providerId);
                userRepository.save(user);
                log.info("Updated existing user {} with OAuth2 provider: {}", user.getUsername(), provider);
            }
            
            CustomUserDetails userDetails = new CustomUserDetails(
                    user.getId(),
                    user.getUsername(),
                    user.getPasswordHash(),
                    true, true, true, true,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getUserType().name()))
            );
            
            String jwtToken = jwtUtil.generateToken(userDetails);
            
            log.info("JWT token generated for OAuth2 user: {}", user.getUsername());
            
            String ipAddress = SecurityAuditService.extractClientIp(request);
            String userAgent = SecurityAuditService.extractUserAgent(request);
            
            securityAuditService.logEvent(
                user.getUsername(),
                SecurityEventType.LOGIN_SUCCESS,
                ipAddress,
                userAgent,
                String.format("OAuth2 login successful via %s", provider),
                true
            );
            
            String frontendUrl = "http://localhost:5173";
            String redirectUrl = String.format("%s/oauth2/callback?token=%s&provider=%s", 
                    frontendUrl, jwtToken, provider);
            
            log.info("Redirecting to frontend: {}", redirectUrl);
            response.sendRedirect(redirectUrl);
            
        } catch (Exception e) {
            log.error("Error processing OAuth2 authentication", e);
            redirectToFrontendWithError(response, "authentication_processing_error");
        }
    }
    
    /**
     * Creates new OAuth2 user.
     */
    private User createNewOAuth2User(String email, String givenName, String familyName, 
                                     String fullName, String provider, String providerId) {
        log.info("Creating new OAuth2 user - Email: {}, Provider: {}", email, provider);
        
        String firstName = givenName;
        String lastName = familyName;
        
        if (firstName == null || firstName.isEmpty()) {
            if (fullName != null && !fullName.isEmpty()) {
                String[] nameParts = fullName.split("\\s+", 2);
                firstName = nameParts[0];
                lastName = nameParts.length > 1 ? nameParts[1] : "";
            } else {
                firstName = email.split("@")[0];
                lastName = "";
            }
        }
        
        if (lastName == null || lastName.isEmpty()) {
            lastName = "";
        }
        
        String baseUsername = email.split("@")[0];
        String username = generateUniqueUsername(baseUsername);
        
        User newUser = User.builder()
                .username(username)
                .email(email)
                .passwordHash("OAUTH2_USER_NO_PASSWORD")
                .firstName(firstName)
                .lastName(lastName)
                .userType(UserType.CUSTOMER)
                .oauthProvider(provider)
                .oauthProviderId(providerId)
                .build();
        
        User savedUser = userRepository.save(newUser);
        log.info("Created new OAuth2 user: {} (ID: {})", savedUser.getUsername(), savedUser.getId());
        
        return savedUser;
    }
    
    /**
     * Generates unique username.
     */
    private String generateUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
    
    /**
     * Determines OAuth2 provider from request.
     */
    private String determineProvider(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        
        if (requestUri.contains("google")) {
            return "google";
        } else if (requestUri.contains("github")) {
            return "github";
        } else if (requestUri.contains("facebook")) {
            return "facebook";
        }
        
        return "unknown";
    }

    
    /**
     * Redirects to frontend with error.
     */
    private void redirectToFrontendWithError(HttpServletResponse response, String error) throws IOException {
        String frontendUrl = "http://localhost:5173";
        String redirectUrl = String.format("%s/login?error=%s", frontendUrl, error);
        response.sendRedirect(redirectUrl);
    }
}
