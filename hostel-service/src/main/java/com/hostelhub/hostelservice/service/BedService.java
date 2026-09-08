package com.hostelhub.hostelservice.service;

import com.hostelhub.hostelservice.dto.request.CreateBedRequest;
import com.hostelhub.hostelservice.dto.request.UpdateBedRequest;
import com.hostelhub.hostelservice.dto.response.BedResponse;
import com.hostelhub.hostelservice.dto.response.InternalBedResponse;
import com.hostelhub.hostelservice.entity.BedStatus;

import java.util.List;
import java.util.UUID;

public interface BedService {

    BedResponse createBed(
            UUID hostelId,
            UUID roomId,
            CreateBedRequest request
    );

    BedResponse getBed(
            UUID hostelId,
            UUID roomId,
            UUID bedId
    );

    List<BedResponse> searchBeds(
            UUID hostelId,
            UUID roomId,
            BedStatus status
    );

    BedResponse updateBed(
            UUID hostelId,
            UUID roomId,
            UUID bedId,
            UpdateBedRequest request
    );

    void deleteBed(
            UUID hostelId,
            UUID roomId,
            UUID bedId
    );

    BedResponse getBed(UUID id);

    BedResponse occupyBed(UUID id);

    BedResponse releaseBed(UUID id);

    InternalBedResponse getInternalBed(UUID bedId);
}