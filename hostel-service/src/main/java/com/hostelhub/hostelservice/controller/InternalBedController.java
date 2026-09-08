package com.hostelhub.hostelservice.controller;

import com.hostelhub.hostelservice.dto.response.BedResponse;
import com.hostelhub.hostelservice.dto.response.InternalBedResponse;
import com.hostelhub.hostelservice.service.BedService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/beds")
@RequiredArgsConstructor
public class InternalBedController {

    private final BedService bedService;

    @GetMapping("/{id}")
    public InternalBedResponse getBed(
            @PathVariable UUID id
    ) {
        return bedService.getInternalBed(id);
    }

    @PutMapping("/{id}/occupy")
    public BedResponse occupyBed(
            @PathVariable UUID id
    ) {
        return bedService.occupyBed(id);
    }

    @PutMapping("/{id}/release")
    public BedResponse releaseBed(
            @PathVariable UUID id
    ) {
        return bedService.releaseBed(id);
    }

}