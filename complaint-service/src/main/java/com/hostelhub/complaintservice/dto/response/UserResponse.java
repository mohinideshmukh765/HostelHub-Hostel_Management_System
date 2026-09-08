package com.hostelhub.complaintservice.dto.response;

import java.util.List;
import java.util.UUID;

public record UserResponse(

        UUID id,

        String firstName,

        String lastName,

        String email,

        List<String> roles,

        Boolean active


) {
}