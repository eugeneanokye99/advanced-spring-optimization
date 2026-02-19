package com.shopjoy.service;

import com.shopjoy.dto.request.ChangePasswordRequest;
import com.shopjoy.dto.request.CreateUserRequest;
import com.shopjoy.dto.request.LoginRequest;
import com.shopjoy.dto.response.LoginResponse;
import com.shopjoy.dto.response.UserResponse;
import com.shopjoy.entity.UserType;
import com.shopjoy.exception.AuthenticationException;
import com.shopjoy.exception.DuplicateResourceException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.exception.ValidationException;

/**
 * Service interface for Authentication-related operations.
 * Handles user registration, login, and password management.
 */
public interface AuthService {

    /**
     * Registers a new user in the system with a specific user type.
     * Validates that username and email are unique before creating the account.
     * Hashes the password before storing it.
     *
     * @param request  the user registration request DTO
     * @param userType the user type to assign (CUSTOMER or ADMIN)
     * @return the created user response DTO
     * @throws DuplicateResourceException if username or email already exists
     * @throws ValidationException        if user data is invalid
     */
    UserResponse registerUser(CreateUserRequest request, UserType userType);

    /**
     * Authenticates a user with username and password.
     * Returns JWT token and user details upon successful authentication.
     *
     * @param request the login request containing username and password
     * @return the login response with JWT token and user details
     * @throws AuthenticationException if credentials are invalid
     */
    LoginResponse login(LoginRequest request);

    /**
     * Changes a user's password.
     *
     * @param userId  the user ID
     * @param request the change password request containing old and new passwords
     * @throws ResourceNotFoundException if user not found
     * @throws AuthenticationException   if old password is incorrect
     * @throws ValidationException       if new password doesn't meet requirements
     */
    void changePassword(Integer userId, ChangePasswordRequest request);

    /**
     * Checks if an email address is already registered.
     *
     * @param email the email to check
     * @return true if email exists, false otherwise
     */
    boolean isEmailTaken(String email);

    /**
     * Checks if a username is already taken.
     *
     * @param username the username to check
     * @return true if username exists, false otherwise
     */
    boolean isUsernameTaken(String username);
}
