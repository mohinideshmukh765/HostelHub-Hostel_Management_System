package com.hostelhub.hostelservice.dto.response;

import com.hostelhub.hostelservice.entity.BedStatus;

import java.util.UUID;

public record InternalBedResponse(

        UUID bedId,

        UUID roomId,

        UUID hostelId,

        UUID adminId,

        BedStatus status

) {
}