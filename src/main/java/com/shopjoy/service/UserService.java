package com.shopjoy.service;

import com.shopjoy.dto.request.UpdateUserRequest;
import com.shopjoy.dto.response.UserResponse;
import com.shopjoy.entity.UserType;
import com.shopjoy.exception.*;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for User-related business operations.
 * Handles user profile management and user queries.
 * Authentication operations are handled by AuthService.
 * 
 * DTO-FIRST DESIGN: All methods accept and return DTOs.
 * Mapping between DTOs and entities is handled internally by the service implementation.
 */
public interface UserService {
    
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
     * Deletes a user from the system.
     * Should check for related data (orders, reviews, etc.) before deletion.
     * 
     * @param userId the user ID
     * @throws ResourceNotFoundException if user not found
     * @throws BusinessException if user has related data that prevents deletion
     */
    void deleteUser(Integer userId);
}
