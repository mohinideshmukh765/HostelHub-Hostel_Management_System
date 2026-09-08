package com.hostelhub.complaintservice.dto.request;

import com.hostelhub.complaintservice.entity.ComplaintStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateComplaintStatusRequest(

        @NotNull(message = "Status is required")
        ComplaintStatus status,

        @Size(max = 2000, message = "Remarks cannot exceed 2000 characters")
        String remarks,

        String rejectionReason

) {
}

