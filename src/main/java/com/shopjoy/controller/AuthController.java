package com.shopjoy.controller;

import com.shopjoy.dto.request.ChangePasswordRequest;
import com.shopjoy.dto.request.CreateUserRequest;
import com.shopjoy.dto.request.LoginRequest;
import com.shopjoy.dto.response.ApiResponse;
import com.shopjoy.dto.response.LoginResponse;
import com.shopjoy.dto.response.UserResponse;
import com.shopjoy.entity.UserType;
import com.shopjoy.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for authentication-related operations.
 */
@Tag(name = "Authentication", description = "APIs for user authentication including registration, login, and password management")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Instantiates a new Auth controller.
     *
     * @param authService the auth service
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

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
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
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
}
