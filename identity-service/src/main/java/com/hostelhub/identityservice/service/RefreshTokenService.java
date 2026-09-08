package com.hostelhub.identityservice.service;

import com.hostelhub.identityservice.entity.RefreshToken;
import com.hostelhub.identityservice.entity.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);


    RefreshToken verifyRefreshToken(String token);

    void revokeRefreshToken(String token);

    void revokeAllUserTokens(User user);


}