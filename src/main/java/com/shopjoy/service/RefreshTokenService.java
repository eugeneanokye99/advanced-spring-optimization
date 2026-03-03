package com.shopjoy.service;

import com.shopjoy.entity.RefreshToken;
import com.shopjoy.entity.User;


public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user, String ipAddress, String userAgent);

}

