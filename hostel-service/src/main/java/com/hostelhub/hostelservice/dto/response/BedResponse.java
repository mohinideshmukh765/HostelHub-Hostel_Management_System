package com.hostelhub.hostelservice.dto.response;

import com.hostelhub.hostelservice.entity.BedStatus;

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