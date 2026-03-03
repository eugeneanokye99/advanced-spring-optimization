package com.shopjoy.service.impl;

import com.shopjoy.entity.RefreshToken;
import com.shopjoy.entity.User;
import com.shopjoy.repository.RefreshTokenRepository;
import com.shopjoy.service.RefreshTokenService;
import com.shopjoy.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user, String ipAddress, String userAgent) {
        String tokenStr = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenStr)
                .user(user)
                .expiryDate(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshExpirationTime() / 1000))
                .createdAt(LocalDateTime.now())
                .revoked(false)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }


}

