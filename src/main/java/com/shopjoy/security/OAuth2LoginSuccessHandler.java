package com.shopjoy.security;

import com.shopjoy.entity.User;
import com.shopjoy.entity.UserType;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
 * Custom OAuth2 login success handler that creates or updates users from OAuth2 providers
 * and generates JWT tokens for authentication.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Called when OAuth2 authentication succeeds.
     * Creates or updates user from OAuth2 profile and redirects with JWT token.
     *
     * @param request        the request which caused the successful authentication
     * @param response       the response
     * @param authentication the <tt>Authentication</tt> object which was created during authentication process
     * @throws IOException      if an input or output error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        log.info("OAuth2 authentication successful for user: {}", authentication.getName());
        
        try {
            // Extract OAuth2User from authentication
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            
            // Get attributes from OAuth2 provider
            Map<String, Object> attributes = oAuth2User.getAttributes();
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            String givenName = (String) attributes.get("given_name");
            String familyName = (String) attributes.get("family_name");
            String providerId = (String) attributes.get("sub");
            String provider = determineProvider(request);
            
            log.info("OAuth2 user details - Email: {}, Name: {}, Provider: {}", email, name, provider);
            
            // Validate email
            if (email == null || email.isEmpty()) {
                log.error("OAuth2 authentication failed: Email not provided by OAuth2 provider");
                redirectToFrontendWithError(response, "email_not_provided");
                return;
            }
            
            // Find or create user
            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createNewOAuth2User(email, givenName, familyName, name, provider, providerId));
            
            // Update OAuth2 info if user exists but wasn't an OAuth2 user
            if (user.getOauthProvider() == null) {
                user.setOauthProvider(provider);
                user.setOauthProviderId(providerId);
                userRepository.save(user);
                log.info("Updated existing user {} with OAuth2 provider: {}", user.getUsername(), provider);
            }
            
            // Create UserDetails for JWT generation
            CustomUserDetails userDetails = new CustomUserDetails(
                    user.getId(),
                    user.getUsername(),
                    user.getPasswordHash(),
                    true, true, true, true,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getUserType().name()))
            );
            
            // Generate JWT token
            String jwtToken = jwtUtil.generateToken(userDetails);
            
            log.info("JWT token generated for OAuth2 user: {}", user.getUsername());
            
            // Redirect to frontend with token
            String frontendUrl = getFrontendBaseUrl();
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
     * Creates a new user from OAuth2 profile data.
     *
     * @param email       the user's email
     * @param givenName   the given name from OAuth2
     * @param familyName  the family name from OAuth2
     * @param fullName    the full name from OAuth2
     * @param provider    the OAuth2 provider (google, github, etc.)
     * @param providerId  the provider-specific user ID
     * @return the newly created user
     */
    private User createNewOAuth2User(String email, String givenName, String familyName, 
                                     String fullName, String provider, String providerId) {
        log.info("Creating new OAuth2 user - Email: {}, Provider: {}", email, provider);
        
        // Parse name into firstName and lastName
        String firstName = givenName;
        String lastName = familyName;
        
        // Fallback if givenName/familyName not provided
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
        
        // Generate unique username from email
        String baseUsername = email.split("@")[0];
        String username = generateUniqueUsername(baseUsername);
        
        // Create new user
        User newUser = User.builder()
                .username(username)
                .email(email)
                .passwordHash("OAUTH2_USER_NO_PASSWORD") // OAuth2 users don't have passwords
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
     * Generates a unique username by appending numbers if necessary.
     *
     * @param baseUsername the base username
     * @return a unique username
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
     * Determines the OAuth2 provider from the request URI.
     *
     * @param request the HTTP request
     * @return the provider name (e.g., "google", "github")
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
     * Gets the frontend base URL from allowed origins.
     *
     * @return the frontend base URL
     */
    private String getFrontendBaseUrl() {
        // Get the first allowed origin as the default frontend URL
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
            String[] origins = allowedOrigins.split(",");
            // Prefer localhost:5173 (Vite default) or localhost:3000 (React default)
            for (String origin : origins) {
                if (origin.contains("5173") || origin.contains("3000")) {
                    return origin.trim();
                }
            }
            return origins[0].trim();
        }
        return "http://localhost:5173";
    }
    
    /**
     * Redirects to frontend with an error message.
     *
     * @param response the HTTP response
     * @param error    the error code
     * @throws IOException if redirect fails
     */
    private void redirectToFrontendWithError(HttpServletResponse response, String error) throws IOException {
        String frontendUrl = getFrontendBaseUrl();
        String redirectUrl = String.format("%s/login?error=%s", frontendUrl, error);
        response.sendRedirect(redirectUrl);
    }
}
