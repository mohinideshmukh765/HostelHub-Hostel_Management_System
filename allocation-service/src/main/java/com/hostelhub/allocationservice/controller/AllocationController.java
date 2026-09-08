package com.hostelhub.allocationservice.controller;

import com.hostelhub.allocationservice.dto.request.CheckoutRequest;
import com.hostelhub.allocationservice.dto.request.CreateAllocationRequest;
import com.hostelhub.allocationservice.dto.response.AllocationResponse;
import com.hostelhub.allocationservice.service.AllocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/allocations")
@RequiredArgsConstructor
public class AllocationController {

    private final AllocationService allocationService;

    @PostMapping
    public ResponseEntity<AllocationResponse> createAllocation(
            @Valid @RequestBody CreateAllocationRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        allocationService.createAllocation(request)
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AllocationResponse> getAllocation(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                allocationService.getAllocation(id)
        );
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AllocationResponse>> getStudentAllocations(
            @PathVariable UUID studentId
    ) {

        return ResponseEntity.ok(
                allocationService.getStudentAllocations(studentId)
        );
    }

    @PostMapping("/{id}/checkout")
    public ResponseEntity<AllocationResponse> checkout(
            @PathVariable UUID id,
            @RequestBody(required = false) CheckoutRequest request
    ) {

        return ResponseEntity.ok(
                allocationService.checkout(id, request)
        );
    }
}