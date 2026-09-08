package com.hostelhub.hostelservice.dto.request;

import com.hostelhub.hostelservice.entity.RoomType;
import jakarta.validation.constraints.*;

public record CreateRoomRequest(

        @NotBlank(message = "Room number is required")
        @Size(max = 20, message = "Room number must not exceed 20 characters")
        String roomNumber,

        @NotNull(message = "Floor is required")
        @Min(value = 1, message = "Floor must be at least 1")
        Integer floor,

        @NotNull(message = "Room type is required")
        RoomType type,

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        @Max(value = 10, message = "Capacity cannot exceed 10")
        Integer capacity

) {
}