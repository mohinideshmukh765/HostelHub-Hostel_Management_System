package com.hostelhub.allocationservice.client;

import com.hostelhub.allocationservice.dto.external.BedResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "hostel-service")
public interface HostelClient {

    @GetMapping("/api/internal/beds/{id}")
    BedResponse getBed(
            @PathVariable UUID id
    );

    @PutMapping("/api/internal/beds/{id}/occupy")
    void occupyBed(
            @PathVariable UUID id
    );

    @PutMapping("/api/internal/beds/{id}/release")
    void releaseBed(
            @PathVariable UUID id
    );
}