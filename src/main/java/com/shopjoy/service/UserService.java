package com.shopjoy.service;

import com.shopjoy.dto.request.CreateUserRequest;
import com.shopjoy.dto.request.UpdateUserRequest;
import com.shopjoy.dto.response.UserResponse;
import com.shopjoy.entity.UserType;
import com.shopjoy.exception.*;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for User-related business operations.
 * Handles user registration, authentication, profile management, and user queries.
 * 
 * DTO-FIRST DESIGN: All methods accept and return DTOs.
 * Mapping between DTOs and entities is handled internally by the service implementation.
 */
public interface UserService {
    
    /**
     * Registers a new user in the system.
     * Validates that username and email are unique before creating the account.
     * Hashes the password before storing it.
     * User type defaults to CUSTOMER.
     * 
     * @param request the user registration request DTO
     * @return the created user response DTO
     * @throws DuplicateResourceException if username or email already exists
     * @throws ValidationException if user data is invalid
     */
    UserResponse registerUser(CreateUserRequest request);
    
    /**
     * Registers a new user in the system with a specific user type.
     * Validates that username and email are unique before creating the account.
     * Hashes the password before storing it.
     * 
     * @param request the user registration request DTO
     * @param userType the user type to assign (CUSTOMER or ADMIN)
     * @return the created user response DTO
     * @throws DuplicateResourceException if username or email already exists
     * @throws ValidationException if user data is invalid
     */
    UserResponse registerUser(CreateUserRequest request, UserType userType);
    
    /**
     * Authenticates a user with username and plain text password.
     * 
     * @param username the username
     * @param password the plain text password
     * @return the authenticated user response DTO
     * @throws AuthenticationException if credentials are invalid
     */
    UserResponse authenticateUser(String username, String password);
    
    /**
     * Retrieves a user by their ID.
     * 
     * @param userId the user ID
     * @return the user response DTO
     * @throws ResourceNotFoundException if user not found
     */
    UserResponse getUserById(Integer userId);

    /**
     * Retrieves multiple users by their IDs in a single batch.
     * Useful for optimizing GraphQL N+1 queries.
     * 
     * @param userIds list of user IDs to retrieve
     * @return list of user response DTOs
     */
    List<UserResponse> getUsersByIds(List<Integer> userIds);
    
    /**
     * Retrieves a user by their email address.
     * 
     * @param email the email address
     * @return Optional containing the user response DTO if found
     */
    Optional<UserResponse> getUserByEmail(String email);
    
    /**
     * Retrieves a user by their username.
     * 
     * @param username the username
     * @return Optional containing the user response DTO if found
     */
    Optional<UserResponse> getUserByUsername(String username);
    
    /**
     * Retrieves all users in the system.
     * 
     * @return list of all user response DTOs
     */
    List<UserResponse> getAllUsers();
    
    /**
     * Retrieves all users of a specific type.
     * 
     * @param userType the user type (CUSTOMER or ADMIN)
     * @return list of user response DTOs with the specified type
     */
    List<UserResponse> getUsersByType(UserType userType);
    
    /**
     * Updates an existing user's profile information.
     * Username and email uniqueness are validated if changed.
     * Password is not updated through this method.
     * 
     * @param userId the user ID
     * @param request the update request DTO
     * @return the updated user response DTO
     * @throws ResourceNotFoundException if user not found
     * @throws DuplicateResourceException if new username/email already exists
     * @throws ValidationException if user data is invalid
     */
    UserResponse updateUserProfile(Integer userId, UpdateUserRequest request);
    
    /**
     * Changes a user's password.
     * 
     * @param userId the user ID
     * @param oldPassword the current password (for verification)
     * @param newPassword the new password (plain text)
     * @throws ResourceNotFoundException if user not found
     * @throws AuthenticationException if old password is incorrect
     * @throws ValidationException if new password doesn't meet requirements
     */
    void changePassword(Integer userId, String oldPassword, String newPassword);
    
    /**
     * Deletes a user from the system.
     * Should check for related data (orders, reviews, etc.) before deletion.
     * 
     * @param userId the user ID
     * @throws ResourceNotFoundException if user not found
     * @throws BusinessException if user has related data that prevents deletion
     */
    void deleteUser(Integer userId);
    
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
