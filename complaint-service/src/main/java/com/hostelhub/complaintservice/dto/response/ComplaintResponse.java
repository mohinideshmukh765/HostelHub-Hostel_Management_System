package com.hostelhub.complaintservice.dto.response;

import com.hostelhub.complaintservice.entity.ComplaintCategory;
import com.hostelhub.complaintservice.entity.ComplaintPriority;
import com.hostelhub.complaintservice.entity.ComplaintStatus;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ComplaintResponse(

        UUID id,

        UUID studentId,

        ComplaintCategory category,

        String title,

        String description,

        ComplaintPriority priority,

        ComplaintStatus status,

        UUID hostelId,

        UUID roomId,

        UUID bedId,

        UUID handledBy,

        UUID closedBy,

        String resolutionRemarks,

        String rejectionReason,

        Boolean escalated,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        LocalDateTime resolvedAt



) {
}