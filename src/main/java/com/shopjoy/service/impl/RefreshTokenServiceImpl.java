package com.shopjoy.service.impl;

import com.shopjoy.entity.RefreshToken;
import com.shopjoy.entity.User;
import com.shopjoy.exception.AuthenticationException;
import com.shopjoy.repository.RefreshTokenRepository;
import com.shopjoy.service.RefreshTokenService;
import com.shopjoy.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
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

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new AuthenticationException("Refresh token has expired");
        }
        return token;
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    @Transactional
    @Scheduled(fixedRate = 3600000)
    public void deleteExpiredTokens() {
        long deletedCount = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        if (deletedCount > 0) {
            log.info("Cleaned up {} expired refresh tokens", deletedCount);
        }
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(User user) {
        refreshTokenRepository.revokeAllUserTokens(user);
        log.info("Revoked all refresh tokens for user: {}", user.getUsername());
    }
}

