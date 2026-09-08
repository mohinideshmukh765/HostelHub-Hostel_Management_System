package com.hostelhub.allocationservice.mapper;

import com.hostelhub.allocationservice.dto.response.AllocationResponse;
import com.hostelhub.allocationservice.entity.Allocation;
import org.springframework.stereotype.Component;

@Component
public class AllocationMapper {

    public AllocationResponse toResponse(Allocation allocation) {

        return new AllocationResponse(
                allocation.getId(),
                allocation.getStudentId(),
                allocation.getBedId(),
                allocation.getAllocatedDate(),
                allocation.getCheckoutDate(),
                allocation.getStatus(),
                allocation.getRemarks(),
                allocation.getCreatedAt(),
                allocation.getUpdatedAt(),
                allocation.getHostelId()
        );
    }
}