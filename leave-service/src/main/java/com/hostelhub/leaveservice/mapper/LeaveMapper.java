package com.hostelhub.leaveservice.mapper;

import com.hostelhub.leaveservice.dto.request.CreateLeaveRequest;
import com.hostelhub.leaveservice.dto.response.LeaveResponse;
import com.hostelhub.leaveservice.entity.Leave;
import org.springframework.stereotype.Component;

@Component
public class LeaveMapper {

    public Leave toEntity(
            CreateLeaveRequest request
    ) {

        Leave leave = new Leave();

        leave.setStartDate(request.startDate());
        leave.setEndDate(request.endDate());
        leave.setReason(request.reason().trim());

        return leave;
    }

    public LeaveResponse toResponse(
            Leave leave
    ) {

        return new LeaveResponse(

                leave.getId(),

                leave.getStudentId(),

                leave.getHostelId(),

                leave.getRoomId(),

                leave.getBedId(),

                leave.getStartDate(),

                leave.getEndDate(),

                leave.getReason(),

                leave.getStatus(),

                leave.getHandledBy(),

                leave.getClosedBy(),

                leave.getRemarks(),

                leave.getRejectionReason(),

                leave.getEscalated(),

                leave.getCreatedAt(),

                leave.getUpdatedAt(),

                leave.getDecisionAt()

        );
    }

}