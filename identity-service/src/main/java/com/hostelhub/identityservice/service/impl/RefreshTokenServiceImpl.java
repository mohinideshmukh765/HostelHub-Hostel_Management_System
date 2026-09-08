package com.hostelhub.identityservice.service.impl;

import com.hostelhub.identityservice.entity.RefreshToken;
import com.hostelhub.identityservice.entity.User;
import com.hostelhub.identityservice.repository.RefreshTokenRepository;
import com.hostelhub.identityservice.security.JwtProperties;
import com.hostelhub.identityservice.service.RefreshTokenService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Override
    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryDate(
                        LocalDateTime.now()
                                .plusSeconds(jwtProperties.refreshTokenExpiration() / 1000)
                )
                .user(user)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new BadCredentialsException("Refresh token revoked");
        }

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Refresh token expired");
        }

        return refreshToken;
    }

    @Override
    public void revokeRefreshToken(String token) {

        RefreshToken refreshToken =
                verifyRefreshToken(token);

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);

    }
    @Override
    public void revokeAllUserTokens(User user) {

        refreshTokenRepository.deleteByUser(user);

    }

}