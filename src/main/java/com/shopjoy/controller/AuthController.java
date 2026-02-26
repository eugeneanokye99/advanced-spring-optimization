package com.shopjoy.controller;

import com.shopjoy.dto.request.ChangePasswordRequest;
import com.shopjoy.dto.request.CreateUserRequest;
import com.shopjoy.dto.request.LoginRequest;
import com.shopjoy.dto.response.ApiResponse;
import com.shopjoy.dto.response.LoginResponse;
import com.shopjoy.dto.response.UserResponse;
import com.shopjoy.entity.SecurityEventType;
import com.shopjoy.entity.UserType;
import com.shopjoy.service.AuthService;
import com.shopjoy.service.SecurityAuditService;
import com.shopjoy.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for authentication-related operations.
 */
@Tag(name = "Authentication", description = "APIs for user authentication including registration, login, and password management")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityAuditService securityAuditService;
    private final TokenBlacklistService tokenBlacklistService;



    /**
     * Register a new user.
     *
     * @param request the registration request
     * @return the response entity with created user
     */
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with the provided details including username, email, and password. User type defaults to CUSTOMER."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or validation error",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "User with this username or email already exists",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse response = authService.registerUser(request, UserType.CUSTOMER);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully"));
    }

    /**
     * Authenticate user and return JWT token with user details.
     *
     * @param request the login request
     * @return the response entity with JWT token and user details
     */
    @Operation(
            summary = "User login",
            description = "Authenticates a user with username and password. Returns JWT token and user details on successful authentication."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest servletRequest) {
        String ipAddress = SecurityAuditService.extractClientIp(servletRequest);
        String userAgent = SecurityAuditService.extractUserAgent(servletRequest);
        
        try {
            LoginResponse response = authService.login(request);
            securityAuditService.logEvent(
                request.getUsername(),
                SecurityEventType.LOGIN_SUCCESS,
                ipAddress,
                userAgent,
                "User logged in successfully",
                true
            );
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (Exception e) {
            securityAuditService.logEvent(
                request.getUsername(),
                SecurityEventType.LOGIN_FAILURE,
                ipAddress,
                userAgent,
                "Login failed: " + e.getMessage(),
                false
            );
            throw e;
        }
    }

    /**
     * Logout user by blacklisting their JWT token.
     *
     * @param request the HTTP servlet request containing the Authorization header
     * @return the response entity
     */
    @Operation(
            summary = "User logout",
            description = "Logs out a user by blacklisting their JWT token. The token will no longer be valid for authentication."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Logout successful",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Missing or invalid Authorization header",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Missing or invalid Authorization header"));
        }
        
        String token = authHeader.substring(7);
        String ipAddress = SecurityAuditService.extractClientIp(request);
        String userAgent = SecurityAuditService.extractUserAgent(request);
        
        tokenBlacklistService.blacklistToken(token);
        
        try {
            String username = SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();
            
            securityAuditService.logEvent(
                username,
                SecurityEventType.LOGOUT,
                ipAddress,
                userAgent,
                "User logged out successfully",
                true
            );
        } catch (Exception e) {
            securityAuditService.logEvent(
                null,
                SecurityEventType.LOGOUT,
                ipAddress,
                userAgent,
                "User logged out (username unavailable)",
                true
            );
        }
        
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }

    /**
     * Change user password.
     *
     * @param userId  the user ID
     * @param request the change password request
     * @return the response entity
     */
    @Operation(
            summary = "Change password",
            description = "Changes the password for an authenticated user. Requires the current password for verification."
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password changed successfully",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Current password is incorrect",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "New password does not meet requirements",
                    content = @Content(mediaType = "application/json")
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.userId")
    @PutMapping("/{userId}/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Parameter(description = "User unique identifier", required = true, example = "1")
            @PathVariable Integer userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }

    /**
     * Check if email is available.
     *
     * @param email the email to check
     * @return the response entity with availability status
     */
    @Operation(
            summary = "Check email availability",
            description = "Checks if an email address is already registered in the system"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Email availability status returned",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmailAvailability(
            @Parameter(description = "Email address to check", required = true, example = "user@example.com")
            @RequestParam String email) {
        boolean taken = authService.isEmailTaken(email);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("available", !taken, "taken", taken),
                taken ? "Email is already taken" : "Email is available"
        ));
    }

    /**
     * Check if username is available.
     *
     * @param username the username to check
     * @return the response entity with availability status
     */
    @Operation(
            summary = "Check username availability",
            description = "Checks if a username is already taken in the system"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Username availability status returned",
                    content = @Content(mediaType = "application/json")
            )
    })
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUsernameAvailability(
            @Parameter(description = "Username to check", required = true, example = "johndoe")
            @RequestParam String username) {
        boolean taken = authService.isUsernameTaken(username);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("available", !taken, "taken", taken),
                taken ? "Username is already taken" : "Username is available"
        ));
    }

    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token using a valid refresh token"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @Parameter(description = "Refresh token", required = true)
            @RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        LoginResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @Operation(
            summary = "Revoke refresh token",
            description = "Revokes a specific refresh token"
    )
    @PostMapping("/revoke")
    public ResponseEntity<ApiResponse<Void>> revokeRefreshToken(
            @RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        authService.revokeRefreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.success(null, "Refresh token revoked successfully"));
    }
}
