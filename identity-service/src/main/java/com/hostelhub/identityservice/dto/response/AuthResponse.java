package com.hostelhub.identityservice.dto.response;

import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
public record AuthResponse(

        String accessToken,

        String refreshToken,

        String tokenType,

        long expiresIn,

        UserInfo user

) {

    @Builder
    public record UserInfo(

            UUID id,

            String firstName,

            String lastName,

            String email,

            List<String> roles

    ) {
    }
}