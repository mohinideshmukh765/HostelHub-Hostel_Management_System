package com.hostelhub.leaveservice.service;

import com.hostelhub.leaveservice.dto.request.CreateLeaveRequest;
import com.hostelhub.leaveservice.dto.request.UpdateLeaveRequest;
import com.hostelhub.leaveservice.dto.request.UpdateLeaveStatusRequest;
import com.hostelhub.leaveservice.dto.response.LeaveResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

public interface LeaveService {

    LeaveResponse getLeave(
            UUID leaveId
    );

    List<LeaveResponse> getMyLeaves(
            UUID studentId
    );

    List<LeaveResponse> getStudentLeaves(
            UUID studentId
    );

    List<LeaveResponse> getHostelLeaves(
            UUID hostelId
    );

    List<LeaveResponse> getAllLeaves();

    List<LeaveResponse> getLeavesByStatus(
            String status
    );

    LeaveResponse updateLeave(
            UUID leaveId,
            UUID studentId,
            UpdateLeaveRequest request
    );

    LeaveResponse acceptLeave(
            UUID leaveId,
            UUID wardenId
    );

    LeaveResponse updateLeaveStatus(
            UUID leaveId,
            UUID wardenId,
            UpdateLeaveStatusRequest request
    );

    void deleteLeave(
            UUID leaveId,
            UUID studentId
    );

    LeaveResponse createLeave(
            UUID studentId,
            @Valid CreateLeaveRequest request
    );
}