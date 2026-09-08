package com.hostelhub.allocationservice.controller;

import com.hostelhub.allocationservice.dto.response.StudentAllocationResponse;
import com.hostelhub.allocationservice.service.AllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/allocations")
@RequiredArgsConstructor

public class InternalAllocationController {

    private final AllocationService allocationService;

    @GetMapping("/student/{studentId}")
    public StudentAllocationResponse getStudentAllocation(
            @PathVariable UUID studentId
    ) {
        return allocationService.getStudentAllocation(studentId);
    }
}