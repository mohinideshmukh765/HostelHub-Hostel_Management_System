package com.hostelhub.hostelservice.dto.request;

import com.hostelhub.hostelservice.entity.HostelStatus;
import com.hostelhub.hostelservice.entity.RoomType;
import jakarta.validation.constraints.*;

public record UpdateRoomRequest(

        @NotBlank(message = "Room number is required")
        @Size(max = 20)
        String roomNumber,

        @NotNull(message = "Floor is required")
        @Min(1)
        Integer floor,

        @NotNull(message = "Room type is required")
        RoomType type,

        @NotNull(message = "Capacity is required")
        @Min(1)
        @Max(10)
        Integer capacity,

        @NotNull(message = "Room status is required")
        HostelStatus status

) {
}