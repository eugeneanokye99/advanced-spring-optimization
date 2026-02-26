package com.shopjoy.service;

import com.shopjoy.entity.RefreshToken;
import com.shopjoy.entity.User;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user, String ipAddress, String userAgent);

    Optional<RefreshToken> findByToken(String token);

    RefreshToken verifyExpiration(RefreshToken token);

    void deleteByUser(User user);

    void deleteExpiredTokens();

    void revokeAllUserTokens(User user);
}

