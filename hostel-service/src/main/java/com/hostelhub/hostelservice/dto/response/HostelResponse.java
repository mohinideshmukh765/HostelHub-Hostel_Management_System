package com.hostelhub.hostelservice.dto.response;

import com.hostelhub.hostelservice.entity.HostelStatus;
import com.hostelhub.hostelservice.entity.HostelType;

import java.time.LocalDateTime;
import java.util.UUID;

public record HostelResponse(

        UUID id,

        String name,

        String description,

        HostelType type,

        HostelStatus status,

        String address,

        String city,

        String state,

        String pincode,

        Integer totalFloors,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        UUID adminId

) {
}