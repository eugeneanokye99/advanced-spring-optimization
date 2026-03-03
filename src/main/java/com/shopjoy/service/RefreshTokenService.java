package com.shopjoy.service;

import com.shopjoy.entity.RefreshToken;
import com.shopjoy.entity.User;

import java.util.Optional;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user, String ipAddress, String userAgent);

}

