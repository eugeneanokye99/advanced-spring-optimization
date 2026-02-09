package com.shopjoy.service.impl;

import com.shopjoy.aspect.Auditable;
import com.shopjoy.dto.mapper.UserMapper;
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
import org.mindrot.jbcrypt.BCrypt;

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
public class UserServiceImpl implements UserService {



    private final UserRepository userRepository;

    /**
     * Instantiates a new User service.
     *
     * @param userRepository the user repository
     */
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional()
    @Auditable(action = "USER_REGISTRATION", description = "Registering new user")
    public UserResponse registerUser(CreateUserRequest request) {
        validateCreateUserRequest(request);

        if (userRepository.usernameExists(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        if (userRepository.emailExists(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = UserMapper.toUser(request);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User createdUser = userRepository.save(user);

        return UserMapper.toUserResponse(createdUser);
    }

    @Override
    public UserResponse authenticateUser(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }

        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password cannot be empty");
        }

        Optional<User> userOpt = userRepository.authenticate(username, password);

        if (userOpt.isEmpty()) {
            throw new AuthenticationException();
        }

        return UserMapper.toUserResponse(userOpt.get());
    }

    @Override
    public UserResponse getUserById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return UserMapper.toUserResponse(user);
    }

    @Override
    public Optional<UserResponse> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserMapper::toUserResponse);
    }

    @Override
    public Optional<UserResponse> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserMapper::toUserResponse);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getUsersByType(UserType userType) {
        if (userType == null) {
            throw new ValidationException("User type cannot be null");
        }
        return userRepository.findByUserType(userType).stream()
                .map(UserMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional()
    public UserResponse updateUserProfile(Integer userId, UpdateUserRequest request) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        validateUpdateUserRequest(request);

        if (request.getEmail() != null && !existingUser.getEmail().equals(request.getEmail())) {
            if (userRepository.emailExists(request.getEmail())) {
                throw new DuplicateResourceException("User", "email", request.getEmail());
            }
        }

        // Apply updates
        UserMapper.updateUserFromRequest(existingUser, request);
        existingUser.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.update(existingUser);

        return UserMapper.toUserResponse(updatedUser);
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

        userRepository.changePassword(userId, newPassword);
    }

    @Override
    @Transactional()
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        userRepository.delete(userId);
    }

    @Override
    public boolean isEmailTaken(String email) {
        return userRepository.emailExists(email);
    }

    @Override
    public boolean isUsernameTaken(String username) {
        return userRepository.usernameExists(username);
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
