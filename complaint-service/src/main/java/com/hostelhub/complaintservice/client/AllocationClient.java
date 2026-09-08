package com.hostelhub.complaintservice.client;

import com.hostelhub.complaintservice.dto.response.StudentAllocationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "allocation-service")
public interface AllocationClient {

    @GetMapping("/api/internal/allocations/student/{studentId}")
    StudentAllocationResponse getStudentAllocation(
            @PathVariable UUID studentId
    );
}
