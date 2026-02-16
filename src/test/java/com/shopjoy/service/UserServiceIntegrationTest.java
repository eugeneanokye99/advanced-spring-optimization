package com.shopjoy.service;

import com.shopjoy.dto.request.CreateUserRequest;
import com.shopjoy.dto.response.UserResponse;
import com.shopjoy.entity.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("Register and Authenticate User - Integration Test")
    void registerAndAuthenticateUser() {
        // 1. Create registration request
        CreateUserRequest request = CreateUserRequest.builder()
                .username("integration_user")
                .email("integration@example.com")
                .password("Password123")
                .firstName("Integration")
                .lastName("Test")
                .build();

        // 2. Register user
        UserResponse registeredUser = userService.registerUser(request, UserType.CUSTOMER);

        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getUsername()).isEqualTo("integration_user");

        // 3. Authenticate user
        UserResponse authenticatedUser = userService.authenticateUser("integration_user", "Password123");

        assertThat(authenticatedUser).isNotNull();
        assertThat(authenticatedUser.getUserId()).isEqualTo(registeredUser.getUserId());
    }
}
