package com.shopjoy.util;

import com.shopjoy.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;
import java.util.Optional;

/**
 * Spring Security utility for current user access.
 */
public final class SecurityUtil {

    private SecurityUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Gets current username.
     */
    public static Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof UserDetails userDetails) {
            return Optional.of(userDetails.getUsername());
        } else if (principal instanceof String username) {
            return Optional.of(username);
        }
        
        return Optional.empty();
    }

    /**
     * Gets current user ID.
     */
    public static Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        Object principal = authentication.getPrincipal();
        
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUserId();
        }
        
        return null;
    }

    /**
     * Checks if the given user ID matches the currently authenticated user.
     *
     * @param userId The user ID to check
     * @return true if the current user's ID matches the given ID, false otherwise
     */
    public static boolean isCurrentUser(Integer userId) {
        Integer currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }

    /**
     * Checks if the currently authenticated user has the ADMIN role.
     *
     * @return true if the current user is an admin, false otherwise
     */
    public static boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).filter(Objects::nonNull)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));
    }

    /**
     * Checks if the currently authenticated user has the CUSTOMER role.
     *
     * @return true if the current user is a customer, false otherwise
     */
    public static boolean isCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_CUSTOMER"));
    }

    /**
     * Checks if the currently authenticated user has any of the specified roles.
     * <p>
     * The "ROLE_" prefix is automatically added if not present.
     *
     * @param roles Variable number of role names (with or without "ROLE_" prefix)
     * @return true if the current user has any of the specified roles, false otherwise
     */
    public static boolean hasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        for (String role : roles) {
            String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            boolean hasRole = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(auth -> auth.equals(roleWithPrefix));
            if (hasRole) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Checks if the current user can access the specified user's data.
     * <p>
     * Access is granted if:
     * - The current user is an admin, OR
     * - The current user's ID matches the specified user ID
     *
     * @param userId The ID of the user whose data is being accessed
     * @return true if access is allowed, false otherwise
     */
    public static boolean canAccessUser(Integer userId) {
        return isAdmin() || isCurrentUser(userId);
    }

    /**
     * Checks if a user is currently authenticated.
     *
     * @return true if there is an authenticated user, false otherwise
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Gets the currently authenticated user's username or null if not authenticated.
     *
     * @return Username or null
     */
    public static String getCurrentUsernameOrNull() {
        return getCurrentUsername().orElse(null);
    }

    /**
     * Requires the current user to be an admin, throwing an exception otherwise.
     *
     * @throws org.springframework.security.access.AccessDeniedException if not an admin
     */
    public static void requireAdmin() {
        if (!isAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException(
                "This operation requires administrator privileges");
        }
    }

    /**
     * Requires the current user to be either an admin or the owner of the resource.
     *
     * @param resourceOwnerId The ID of the resource owner
     * @throws org.springframework.security.access.AccessDeniedException if access is denied
     */
    public static void requireAdminOrOwner(Integer resourceOwnerId) {
        if (!canAccessUser(resourceOwnerId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                "You do not have permission to access this resource");
        }
    }
}
