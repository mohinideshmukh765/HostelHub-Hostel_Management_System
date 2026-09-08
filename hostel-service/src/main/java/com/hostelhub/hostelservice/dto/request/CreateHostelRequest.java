package com.hostelhub.hostelservice.dto.request;

import com.hostelhub.hostelservice.entity.HostelType;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreateHostelRequest(

        @NotBlank(message = "Hostel name is required")
        @Size(max = 150, message = "Hostel name must not exceed 150 characters")
        String name,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotNull(message = "Hostel type is required")
        HostelType type,

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
        @Min(value = 1, message = "Total floors must be at least 1")
        @Max(value = 100, message = "Total floors cannot exceed 100")
        Integer totalFloors,

        UUID adminId

) {
}