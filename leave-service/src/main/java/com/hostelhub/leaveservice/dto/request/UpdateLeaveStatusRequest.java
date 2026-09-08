package com.hostelhub.leaveservice.dto.request;

import com.hostelhub.leaveservice.entity.LeaveStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateLeaveStatusRequest(

        @NotNull
        LeaveStatus status,

        String remarks


) {
}