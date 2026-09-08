package com.hostelhub.hostelservice.dto.request;

import com.hostelhub.hostelservice.entity.BedStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateBedRequest(

        @NotBlank(message = "Bed number is required")
        @Size(max = 20)
        String bedNumber,

        @NotNull(message = "Bed status is required")
        BedStatus status

) {
}