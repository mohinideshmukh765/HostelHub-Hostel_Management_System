package com.hostelhub.allocationservice.service;

import com.hostelhub.allocationservice.dto.request.CheckoutRequest;
import com.hostelhub.allocationservice.dto.request.CreateAllocationRequest;
import com.hostelhub.allocationservice.dto.response.AllocationResponse;
import com.hostelhub.allocationservice.dto.response.StudentAllocationResponse;

import java.util.List;
import java.util.UUID;

public interface AllocationService {

    AllocationResponse createAllocation(
            CreateAllocationRequest request
    );

    AllocationResponse getAllocation(
            UUID id
    );

    List<AllocationResponse> getStudentAllocations(
            UUID studentId
    );

    AllocationResponse checkout(
            UUID id,
            CheckoutRequest request
    );

    StudentAllocationResponse getStudentAllocation(UUID studentId);

}