package com.hostelhub.hostelservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBedRequest(

        @NotBlank(message = "Bed number is required")
        @Size(max = 20, message = "Bed number must not exceed 20 characters")
        String bedNumber

) {
}