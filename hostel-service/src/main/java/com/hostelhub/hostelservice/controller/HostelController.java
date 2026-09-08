package com.hostelhub.hostelservice.controller;

import com.hostelhub.hostelservice.entity.HostelStatus;
import com.hostelhub.hostelservice.entity.HostelType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.RequestParam;
import com.hostelhub.hostelservice.dto.request.CreateHostelRequest;
import com.hostelhub.hostelservice.dto.request.UpdateHostelRequest;
import com.hostelhub.hostelservice.dto.response.HostelResponse;
import com.hostelhub.hostelservice.service.HostelService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/hostels")
@RequiredArgsConstructor

public class HostelController {

    private final HostelService hostelService;

    @PostMapping
    public ResponseEntity<HostelResponse> createHostel(
            @Valid
            @RequestBody
            CreateHostelRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(hostelService.createHostel(request));
    }

    @GetMapping("/{hostelId}")
    public ResponseEntity<HostelResponse> getHostel(
            @PathVariable UUID hostelId
    ) {

        return ResponseEntity.ok(
                hostelService.getHostel(hostelId)
        );
    }

    @GetMapping
    public ResponseEntity<Page<HostelResponse>> searchHostels(

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            String city,

            @RequestParam(required = false)
            HostelType type,

            @RequestParam(required = false)
            HostelStatus status,

            @PageableDefault(
                    size = 10,
                    sort = "name"
            )
            Pageable pageable

    ) {

        return ResponseEntity.ok(
                hostelService.searchHostels(
                        search,
                        city,
                        type,
                        status,
                        pageable
                )
        );
    }

    @PutMapping("/{hostelId}")
    public ResponseEntity<HostelResponse> updateHostel(
            @PathVariable UUID hostelId,

            @Valid
            @RequestBody
            UpdateHostelRequest request
    ) {

        return ResponseEntity.ok(
                hostelService.updateHostel(
                        hostelId,
                        request
                )
        );
    }

    @DeleteMapping("/{hostelId}")
    public ResponseEntity<Void> deleteHostel(
            @PathVariable UUID hostelId
    ) {

        hostelService.deleteHostel(hostelId);

        return ResponseEntity.noContent().build();
    }
}