package com.hostelhub.allocationservice.dto.response;

import java.util.UUID;
public record StudentAllocationResponse(

        UUID studentId,

        UUID hostelId,

        UUID roomId,

        UUID bedId

) {
}