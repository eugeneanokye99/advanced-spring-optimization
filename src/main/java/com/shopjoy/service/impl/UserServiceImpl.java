package com.shopjoy.service.impl;

import com.shopjoy.dto.mapper.UserMapperStruct;
import com.shopjoy.dto.request.UpdateUserRequest;
import com.shopjoy.dto.response.UserResponse;
import com.shopjoy.entity.User;
import com.shopjoy.entity.UserType;
import com.shopjoy.exception.DuplicateResourceException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.exception.ValidationException;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.service.UserService;
import com.shopjoy.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
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
    @Cacheable(value = "userProfile", key = "#userId", unless = "#result == null")
    public UserResponse getUserById(Integer userId) {
        if (!SecurityUtil.canAccessUser(userId)) {
            throw new AccessDeniedException("You do not have permission to access this user profile");
        }
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
    @Caching(
        put = { @CachePut(value = "userProfile", key = "#userId", cacheManager = "cacheManager") },
        evict = { @CacheEvict(value = {"userProfileEmail", "userProfileUsername", "usersByIds"}, allEntries = true, cacheManager = "cacheManager") }
    )
    public UserResponse updateUserProfile(Integer userId, UpdateUserRequest request) {
        if (!SecurityUtil.canAccessUser(userId)) {
            throw new AccessDeniedException("You do not have permission to update this user profile");
        }
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        validateUpdateUserRequest(request);

        if (request.getEmail() != null && !existingUser.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("User", "email", request.getEmail());
            }
        }

        userMapper.updateUserFromRequest(request, existingUser);
        existingUser.setUpdatedAt(LocalDateTime.now());

        User updatedUser = userRepository.save(existingUser);

        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional()
    @Caching(evict = {
        @CacheEvict(value = {"userProfile", "userProfileEmail", "userProfileUsername", "usersByIds"}, allEntries = true, cacheManager = "cacheManager")
    })
    public void deleteUser(Integer userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }

        userRepository.deleteById(userId);
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
}
