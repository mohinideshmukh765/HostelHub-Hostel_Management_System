package com.hostelhub.leaveservice.controller;

import com.hostelhub.leaveservice.dto.request.CreateLeaveRequest;
import com.hostelhub.leaveservice.dto.request.UpdateLeaveRequest;
import com.hostelhub.leaveservice.dto.request.UpdateLeaveStatusRequest;
import com.hostelhub.leaveservice.dto.response.LeaveResponse;
import com.hostelhub.leaveservice.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping
    public LeaveResponse createLeave(
            @RequestParam UUID studentId,
            @Valid @RequestBody CreateLeaveRequest request
    ) {
        return leaveService.createLeave(studentId, request);
    }

    @GetMapping("/{leaveId}")
    public LeaveResponse getLeave(@PathVariable UUID leaveId) {
        return leaveService.getLeave(leaveId);
    }

    @GetMapping("/student/{studentId}")
    public List<LeaveResponse> getStudentLeaves(@PathVariable UUID studentId) {
        return leaveService.getStudentLeaves(studentId);
    }

    @GetMapping("/hostel/{hostelId}")
    public List<LeaveResponse> getHostelLeaves(@PathVariable UUID hostelId) {
        return leaveService.getHostelLeaves(hostelId);
    }

    @GetMapping
    public List<LeaveResponse> getAllLeaves() {
        return leaveService.getAllLeaves();
    }

    @GetMapping(params = "status")
    public List<LeaveResponse> getLeavesByStatus(@RequestParam String status) {
        return leaveService.getLeavesByStatus(status);
    }

    @PutMapping("/{leaveId}")
    public LeaveResponse updateLeave(
            @PathVariable UUID leaveId,
            @RequestParam UUID studentId,
            @Valid @RequestBody UpdateLeaveRequest request
    ) {
        return leaveService.updateLeave(leaveId, studentId, request);
    }

    @DeleteMapping("/{leaveId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLeave(
            @PathVariable UUID leaveId,
            @RequestParam UUID studentId
    ) {
        leaveService.deleteLeave(leaveId, studentId);
    }

    @PutMapping("/{leaveId}/accept")
    public LeaveResponse acceptLeave(
            @PathVariable UUID leaveId,
            @RequestParam UUID wardenId
    ) {
        return leaveService.acceptLeave(leaveId, wardenId);
    }

    @PutMapping("/{leaveId}/status")
    public LeaveResponse updateLeaveStatus(
            @PathVariable UUID leaveId,
            @RequestParam UUID wardenId,
            @Valid @RequestBody UpdateLeaveStatusRequest request
    ) {
        return leaveService.updateLeaveStatus(leaveId, wardenId, request);
    }
}