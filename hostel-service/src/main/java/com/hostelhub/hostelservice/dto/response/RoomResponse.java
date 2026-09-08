package com.hostelhub.hostelservice.dto.response;

import com.hostelhub.hostelservice.entity.HostelStatus;
import com.hostelhub.hostelservice.entity.RoomType;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoomResponse(

        UUID id,

        UUID hostelId,

        String roomNumber,

        Integer floor,

        RoomType type,

        Integer capacity,

        HostelStatus status,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

){
}