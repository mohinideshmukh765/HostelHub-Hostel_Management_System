package com.hostelhub.identityservice.dto.request;

public record ChangePasswordRequest(

        String currentPassword,

        String newPassword

) {
}