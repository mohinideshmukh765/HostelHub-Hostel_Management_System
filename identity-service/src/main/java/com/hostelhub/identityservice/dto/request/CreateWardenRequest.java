package com.hostelhub.identityservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateWardenRequest(

        @NotBlank
        @Size(max = 150)
        String firstName,

        @NotBlank
        @Size(max = 150)
        String lastName,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        String password,

        /**
         * Optional: the hostel this warden is assigned to.
         * If provided, it is persisted on User.hostelId so that
         * complaint-service and leave-service can validate hostel-scope.
         */
        UUID hostelId

) {
}