package com.hostelhub.allocationservice.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAllocationRequest(

        @NotNull(message = "Student ID is required")
        UUID studentId,

        @NotNull(message = "Bed ID is required")
        UUID bedId,

        String remarks

) {
}