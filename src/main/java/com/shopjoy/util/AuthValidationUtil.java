package com.shopjoy.util;

import com.shopjoy.dto.request.CreateUserRequest;
import com.shopjoy.dto.request.LoginRequest;
import com.shopjoy.exception.ValidationException;

/**
 * Utility class for authentication validations.
 */
public final class AuthValidationUtil {

    private AuthValidationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Validates user registration request.
     */
    public static void validateCreateUserRequest(CreateUserRequest request) {
        if (request == null) {
            throw new ValidationException("User data cannot be null");
        }

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new ValidationException("username", "must not be empty");
        }

        if (request.getUsername().length() < 3 || request.getUsername().length() > 50) {
            throw new ValidationException("username", "must be between 3 and 50 characters");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("email", "must not be empty");
        }

        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("email", "must be a valid email address");
        }

        validatePassword(request.getPassword());
    }

    /**
     * Validates a login request.
     *
     * @param request the login request to validate
     * @throws ValidationException if validation fails
     */
    public static void validateLoginRequest(LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }

        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ValidationException("Password cannot be empty");
        }
    }

    /**
     * Validates a password meets security requirements.
     *
     * @param password the password to validate
     * @throws ValidationException if validation fails
     */
    public static void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("password", "must not be empty");
        }

        if (password.length() < 8) {
            throw new ValidationException("password", "must be at least 8 characters long");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new ValidationException("password", "must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new ValidationException("password", "must contain at least one lowercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new ValidationException("password", "must contain at least one digit");
        }
    }
}
