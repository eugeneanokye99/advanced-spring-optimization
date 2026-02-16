package com.shopjoy.service.impl;

import com.shopjoy.aspect.Auditable;
import com.shopjoy.dto.mapper.UserMapperStruct;
import com.shopjoy.dto.request.CreateUserRequest;
import com.shopjoy.dto.request.UpdateUserRequest;
import com.shopjoy.dto.response.UserResponse;
import com.shopjoy.entity.User;
import com.shopjoy.entity.UserType;
import com.shopjoy.exception.AuthenticationException;
import com.shopjoy.exception.DuplicateResourceException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.exception.ValidationException;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.service.UserService;
import lombok.AllArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The type User service.
 */
@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapperStruct userMapper;

    @Override
    @Transactional
    @Auditable(action = "USER_REGISTRATION", description = "Registering new user")
    public UserResponse registerUser(CreateUserRequest request) {
        validateCreateUserRequest(request);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = userMapper.toUser(request);
        // Hash the password with BCrypt before saving
        user.setPasswordHash(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User createdUser = userRepository.save(user);

        return userMapper.toUserResponse(createdUser);
    }

    @Override
    @Transactional
    @Auditable(action = "USER_REGISTRATION", description = "Registering new user with specific type")
    public UserResponse registerUser(CreateUserRequest request, UserType userType) {
        validateCreateUserRequest(request);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Use the mapper method that accepts userType parameter
        User user = userMapper.toUser(request, userType);
        // Hash the password with BCrypt before saving
        user.setPasswordHash(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User createdUser = userRepository.save(user);

        return userMapper.toUserResponse(createdUser);
    }

    @Override
    public UserResponse authenticateUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }

        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password cannot be empty");
        }

        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            throw new AuthenticationException();
        }

        User user = userOpt.get();
        
        // Verify password using BCrypt
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new AuthenticationException();
        }

        return userMapper.toUserResponse(user);
    }

    @Override
    @Cacheable(value = "userProfile", key = "#userId", unless = "#result == null")
    public UserResponse getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return userMapper.toUserResponse(user);
    }

    @Override
    @Cacheable(value = "usersByIds")
    public List<UserResponse> getUsersByIds(List<Integer> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        // Remove duplicates and call repository
        List<Integer> distinctIds = userIds.stream()
                .distinct()
                .filter(java.util.Objects::nonNull)
                .toList();
        
        List<User> users = userRepository.findAllById(distinctIds);
        return users.stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    @Override
    @Cacheable(value = "userProfileEmail", key = "#email", unless = "#result == null")
    public Optional<UserResponse> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toUserResponse);
    }

    @Override
    @Cacheable(value = "userProfileUsername", key = "#username", unless = "#result == null")
    public Optional<UserResponse> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(userMapper::toUserResponse);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getUsersByType(UserType userType) {
        if (userType == null) {
            throw new ValidationException("User type cannot be null");
        }
        return userRepository.findByUserType(userType).stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = {"userProfile", "userProfileEmail", "userProfileUsername", "usersByIds"}, allEntries = true)
    })
    public UserResponse updateUserProfile(Integer userId, UpdateUserRequest request) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        validateUpdateUserRequest(request);

        if (request.getEmail() != null && !existingUser.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("User", "email", request.getEmail());
            }
        }

        // Apply updates
        userMapper.updateUserFromRequest(request, existingUser);
        existingUser.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(existingUser);

        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional()
    public void changePassword(Integer userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!BCrypt.checkpw(oldPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        validatePassword(newPassword);

        // Hash the new password before saving
        String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPasswordHash(hashedNewPassword);
        userRepository.save(user);
    }

    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = {"userProfile", "userProfileEmail", "userProfileUsername", "usersByIds"}, allEntries = true)
    })
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        userRepository.deleteById(userId);
    }

    @Override
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    private void validateCreateUserRequest(CreateUserRequest request) {
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

    private void validateUpdateUserRequest(UpdateUserRequest request) {
        if (request == null) {
            throw new ValidationException("Update data cannot be null");
        }

        if (request.getEmail() != null) {
            if (request.getEmail().trim().isEmpty()) {
                throw new ValidationException("email", "must not be empty");
            }

            if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                throw new ValidationException("email", "must be a valid email address");
            }
        }
    }

    private void validatePassword(String password) {
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
