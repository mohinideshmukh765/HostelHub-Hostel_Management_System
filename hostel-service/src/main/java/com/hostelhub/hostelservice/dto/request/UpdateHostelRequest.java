package com.hostelhub.hostelservice.dto.request;

import com.hostelhub.hostelservice.entity.HostelStatus;
import com.hostelhub.hostelservice.entity.HostelType;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record UpdateHostelRequest(

        @NotBlank(message = "Hostel name is required")
        @Size(max = 150)
        String name,

        @Size(max = 500)
        String description,

        @NotNull(message = "Hostel type is required")
        HostelType type,

        @NotNull(message = "Hostel status is required")
        HostelStatus status,

        @NotBlank(message = "Address is required")
        @Size(max = 255)
        String address,

        @NotBlank(message = "City is required")
        @Size(max = 100)
        String city,

        @NotBlank(message = "State is required")
        @Size(max = 100)
        String state,

        @NotBlank(message = "Pincode is required")
        @Pattern(
                regexp = "^[1-9][0-9]{5}$",
                message = "Invalid Indian pincode"
        )
        String pincode,

        @NotNull(message = "Total floors is required")
        @Min(1)
        @Max(100)
        Integer totalFloors,

        UUID adminId

) {
}