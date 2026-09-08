package com.hostelhub.allocationservice.dto.response;

import com.hostelhub.allocationservice.entity.AllocationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record AllocationResponse(

        UUID id,

        UUID studentId,

        UUID bedId,

        LocalDate allocatedDate,

        LocalDate checkoutDate,

        AllocationStatus status,

        String remarks,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        UUID hostelId

) {
}