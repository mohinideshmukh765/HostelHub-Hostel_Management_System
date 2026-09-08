package com.hostelhub.hostelservice.controller;

import com.hostelhub.hostelservice.dto.request.CreateBedRequest;
import com.hostelhub.hostelservice.dto.request.UpdateBedRequest;
import com.hostelhub.hostelservice.dto.response.BedResponse;
import com.hostelhub.hostelservice.entity.BedStatus;
import com.hostelhub.hostelservice.service.BedService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/hostels/{hostelId}/rooms/{roomId}/beds"
)
@RequiredArgsConstructor
public class BedController {

    private final BedService bedService;

    @PostMapping
    public ResponseEntity<BedResponse> createBed(

            @PathVariable UUID hostelId,

            @PathVariable UUID roomId,

            @Valid
            @RequestBody
            CreateBedRequest request

    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        bedService.createBed(
                                hostelId,
                                roomId,
                                request
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<BedResponse>> searchBeds(

            @PathVariable UUID hostelId,

            @PathVariable UUID roomId,

            @RequestParam(required = false)
            BedStatus status

    ) {

        return ResponseEntity.ok(
                bedService.searchBeds(
                        hostelId,
                        roomId,
                        status
                )
        );
    }

    @GetMapping("/{bedId}")
    public ResponseEntity<BedResponse> getBed(

            @PathVariable UUID hostelId,

            @PathVariable UUID roomId,

            @PathVariable UUID bedId

    ) {

        return ResponseEntity.ok(
                bedService.getBed(
                        hostelId,
                        roomId,
                        bedId
                )
        );
    }

    @PutMapping("/{bedId}")
    public ResponseEntity<BedResponse> updateBed(

            @PathVariable UUID hostelId,

            @PathVariable UUID roomId,

            @PathVariable UUID bedId,

            @Valid
            @RequestBody
            UpdateBedRequest request

    ) {

        return ResponseEntity.ok(
                bedService.updateBed(
                        hostelId,
                        roomId,
                        bedId,
                        request
                )
        );
    }

    @DeleteMapping("/{bedId}")
    public ResponseEntity<Void> deleteBed(

            @PathVariable UUID hostelId,

            @PathVariable UUID roomId,

            @PathVariable UUID bedId

    ) {

        bedService.deleteBed(
                hostelId,
                roomId,
                bedId
        );

        return ResponseEntity.noContent().build();
    }
}