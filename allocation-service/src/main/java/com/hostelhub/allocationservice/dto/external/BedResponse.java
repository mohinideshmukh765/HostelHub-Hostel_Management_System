package com.hostelhub.allocationservice.dto.external;

import java.time.LocalDateTime;
import java.util.UUID;

public record BedResponse(

        UUID id,

        UUID hostelId,

        UUID roomId,

        String bedNumber,

        BedStatus status,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}