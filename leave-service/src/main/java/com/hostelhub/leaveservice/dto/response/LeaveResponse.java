package com.hostelhub.leaveservice.dto.response;

import com.hostelhub.leaveservice.entity.LeaveStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LeaveResponse(

        UUID id,

        UUID studentId,

        UUID hostelId,

        UUID roomId,

        UUID bedId,

        LocalDate startDate,

        LocalDate endDate,

        String reason,

        LeaveStatus status,

        UUID handledBy,

        UUID approvedBy,

        String remarks,

        String rejectionReason,

        Boolean escalated,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        LocalDateTime decisionAt

) {
}