package com.shopjoy.service.impl;

import com.shopjoy.aspect.Auditable;
import com.shopjoy.dto.mapper.UserMapperStruct;
import com.shopjoy.dto.request.ChangePasswordRequest;
import com.shopjoy.dto.request.CreateUserRequest;
import com.shopjoy.dto.request.LoginRequest;
import com.shopjoy.dto.response.LoginResponse;
import com.shopjoy.dto.response.UserResponse;
import com.shopjoy.entity.RefreshToken;
import com.shopjoy.entity.User;
import com.shopjoy.entity.UserType;
import com.shopjoy.exception.AuthenticationException;
import com.shopjoy.exception.DuplicateResourceException;
import com.shopjoy.exception.RateLimitExceededException;
import com.shopjoy.exception.ResourceNotFoundException;
import com.shopjoy.repository.RefreshTokenRepository;
import com.shopjoy.repository.UserRepository;
import com.shopjoy.security.CustomUserDetails;
import com.shopjoy.service.AuthService;
import com.shopjoy.service.RateLimitService;
import com.shopjoy.util.AuthValidationUtil;
import com.shopjoy.util.JwtUtil;
import com.shopjoy.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

/**
 * Implementation of AuthService for authentication-related operations.
 */
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserMapperStruct userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RateLimitService rateLimitService;

    @Override
    @Auditable(action = "USER_REGISTRATION", description = "Registering new user")
    public UserResponse registerUser(CreateUserRequest request, UserType userType) {
        AuthValidationUtil.validateCreateUserRequest(request);

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("User", "username", request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        User user = userMapper.toUser(request, userType);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User createdUser = userRepository.save(user);

        return userMapper.toUserResponse(createdUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        AuthValidationUtil.validateLoginRequest(request);
        
        String clientIp = extractClientIp();
        
        if (rateLimitService.isRateLimited(request.getUsername(), clientIp)) {
            long retryAfter = rateLimitService.getRetryAfterSeconds(request.getUsername(), clientIp);
            throw new RateLimitExceededException(
                "Too many failed login attempts. Please try again in " + (retryAfter / 60) + " minutes.",
                retryAfter
            );
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            assert userDetails != null;
            String jwtToken = jwtUtil.generateToken(userDetails);
            
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", request.getUsername()));

            String refreshTokenStr = createRefreshToken(user, clientIp);

            rateLimitService.resetAttempts(request.getUsername(), clientIp);

            return LoginResponse.builder()
                    .token(jwtToken)
                    .refreshToken(refreshTokenStr)
                    .tokenType("Bearer")
                    .expiresIn(jwtUtil.getExpirationTime())
                    .build();

        } catch (org.springframework.security.core.AuthenticationException e) {
            rateLimitService.recordLoginAttempt(request.getUsername(), clientIp);
            throw new AuthenticationException("Invalid username or password");
        }
    }
    
    private String extractClientIp() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            return "0.0.0.0";
        }
        return "0.0.0.0";
    }

    @Override
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        if (!SecurityUtil.canAccessUser(userId)) {
            throw new AccessDeniedException("You do not have permission to change this user's password");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if user is OAuth2 user (no password set)
        if (user.getPasswordHash() == null) {
            throw new AuthenticationException(
                "Cannot change password for OAuth2 accounts. This account uses " + 
                (user.getOauthProvider() != null ? user.getOauthProvider() : "OAuth2") + " authentication."
            );
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        AuthValidationUtil.validatePassword(request.getNewPassword());

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new AuthenticationException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new AuthenticationException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        CustomUserDetails userDetails = new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                true, true, true, true,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getUserType().name()))
        );

        String newAccessToken = jwtUtil.generateToken(userDetails);
        String newRefreshToken = createRefreshToken(user, refreshToken.getIpAddress());

        refreshTokenRepository.delete(refreshToken);

        return LoginResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .build();
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new AuthenticationException("Invalid refresh token"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public void revokeAllUserRefreshTokens(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        refreshTokenRepository.revokeAllUserTokens(user);
    }

    private String createRefreshToken(User user, String ipAddress) {
        String tokenStr = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenStr)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshExpirationTime() / 1000))
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .ipAddress(ipAddress)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenStr;
    }
}
