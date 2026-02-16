package com.shopjoy.service.impl;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapperStruct userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponse userResponse;
    private CreateUserRequest createRequest;

    @BeforeEach
    void setUp() {
        String hashedPassword = BCrypt.hashpw("Password123", BCrypt.gensalt());
        user = User.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .passwordHash(hashedPassword)
                .userType(UserType.CUSTOMER)
                .build();

        userResponse = new UserResponse();
        userResponse.setUserId(1);
        userResponse.setUsername("testuser");
        userResponse.setEmail("test@example.com");

        createRequest = CreateUserRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123")
                .build();
    }

    @Test
    @DisplayName("Register User - Success")
    void registerUser_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toUser(createRequest)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.registerUser(createRequest);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register User with Type - Success")
    void registerUserWithType_Success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toUser(createRequest, UserType.ADMIN)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.registerUser(createRequest, UserType.ADMIN);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Register User - Duplicate Username")
    void registerUser_DuplicateUsername() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(createRequest))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Authenticate - Success")
    void authenticateUser_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.authenticateUser("testuser", "Password123");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Authenticate - User Not Found")
    void authenticateUser_NotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.authenticateUser("nonexistent", "Password123"))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("Authenticate - Invalid Password")
    void authenticateUser_InvalidPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.authenticateUser("testuser", "WrongPass123"))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("Get User By Id - Success")
    void getUserById_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(1);

        assertThat(result.getUserId()).isEqualTo(1);
    }

    @Test
    @DisplayName("Get User By Id - Not Found")
    void getUserById_NotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Update Profile - Success")
    void updateUserProfile_Success() {
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setEmail("new@example.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.updateUserProfile(1, updateRequest);

        assertThat(result).isNotNull();
        verify(userMapper).updateUserFromRequest(eq(updateRequest), any(User.class));
    }

    @Test
    @DisplayName("Change Password - Success")
    void changePassword_Success() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        
        userService.changePassword(1, "Password123", "NewPassword123");
        
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Delete User - Success")
    void deleteUser_Success() {
        when(userRepository.existsById(1)).thenReturn(true);
        
        userService.deleteUser(1);
        
        verify(userRepository).deleteById(1);
    }

    @Test
    @DisplayName("Delete User - Not Found")
    void deleteUser_NotFound() {
        when(userRepository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Get Users By Type - Success")
    void getUsersByType_Success() {
        when(userRepository.findByUserType(UserType.CUSTOMER)).thenReturn(Collections.singletonList(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        List<UserResponse> results = userService.getUsersByType(UserType.CUSTOMER);

        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("Validate Password - Failure Cases")
    void validatePassword_Failures() {
        // Short password
        createRequest.setPassword("Short1!");
        assertThatThrownBy(() -> userService.registerUser(createRequest))
                .isInstanceOf(ValidationException.class);

        // No uppercase
        createRequest.setPassword("password123");
        assertThatThrownBy(() -> userService.registerUser(createRequest))
                .isInstanceOf(ValidationException.class);

        // No lowercase
        createRequest.setPassword("PASSWORD123");
        assertThatThrownBy(() -> userService.registerUser(createRequest))
                .isInstanceOf(ValidationException.class);

        // No digit
        createRequest.setPassword("Password!");
        assertThatThrownBy(() -> userService.registerUser(createRequest))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("Get Users By Ids - Success")
    void getUsersByIds_Success() {
        List<Integer> ids = Arrays.asList(1, 2, 1); // Testing distinct logic
        when(userRepository.findAllById(anyList())).thenReturn(Collections.singletonList(user));
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        List<UserResponse> results = userService.getUsersByIds(ids);

        assertThat(results).hasSize(1);
        verify(userRepository).findAllById(argThat(list -> ((List<?>) list).size() == 2));
    }
}
