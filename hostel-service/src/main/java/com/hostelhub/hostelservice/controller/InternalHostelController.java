package com.hostelhub.hostelservice.controller;


import com.hostelhub.hostelservice.dto.response.HostelResponse;
import com.hostelhub.hostelservice.service.HostelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/hostels")
@RequiredArgsConstructor
public class InternalHostelController {

    private final HostelService hostelService;

    @GetMapping("/{hostelId}")
    public HostelResponse getHostel(
            @PathVariable UUID hostelId
    ) {
        return hostelService.getHostel(hostelId);
    }

}