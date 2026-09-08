package com.hostelhub.allocationservice.dto.external;

import java.util.List;
import java.util.UUID;

public record StudentResponse(

        UUID id,

        String firstName,

        String lastName,

        String email,

        List<String> roles,

        boolean active

) {
}