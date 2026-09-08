package com.hostelhub.hostelservice.service;

import com.hostelhub.hostelservice.dto.request.CreateHostelRequest;
import com.hostelhub.hostelservice.dto.request.UpdateHostelRequest;
import com.hostelhub.hostelservice.dto.response.HostelResponse;
import com.hostelhub.hostelservice.entity.HostelStatus;
import com.hostelhub.hostelservice.entity.HostelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface HostelService {

    HostelResponse createHostel(
            CreateHostelRequest request
    );

    HostelResponse getHostel(
            UUID hostelId
    );

    Page<HostelResponse> searchHostels(
            String search,
            String city,
            HostelType type,
            HostelStatus status,
            Pageable pageable
    );

    HostelResponse updateHostel(
            UUID hostelId,
            UpdateHostelRequest request
    );

    void deleteHostel(
            UUID hostelId
    );
}