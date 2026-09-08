package com.hostelhub.leaveservice.service.impl;

import com.hostelhub.leaveservice.client.AllocationClient;
import com.hostelhub.leaveservice.client.IdentityClient;
import com.hostelhub.leaveservice.dto.request.CreateLeaveRequest;
import com.hostelhub.leaveservice.dto.request.UpdateLeaveRequest;
import com.hostelhub.leaveservice.dto.request.UpdateLeaveStatusRequest;
import com.hostelhub.leaveservice.dto.response.LeaveResponse;
import com.hostelhub.leaveservice.dto.response.StudentAllocationResponse;
import com.hostelhub.leaveservice.dto.response.UserResponse;
import com.hostelhub.leaveservice.entity.Leave;
import com.hostelhub.leaveservice.entity.LeaveStatus;
import com.hostelhub.leaveservice.exception.BusinessException;
import com.hostelhub.leaveservice.exception.ResourceNotFoundException;
import com.hostelhub.leaveservice.mapper.LeaveMapper;
import com.hostelhub.leaveservice.repository.LeaveRepository;
import com.hostelhub.leaveservice.service.LeaveService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final LeaveMapper leaveMapper;
    private final IdentityClient identityClient;
    private final AllocationClient allocationClient;

    // =========================================================
    // STUDENT - CREATE LEAVE
    // =========================================================

    @Override
    public LeaveResponse createLeave(UUID studentId, CreateLeaveRequest request) {

        UserResponse student = fetchUser(studentId);
        validateStudent(student);

        StudentAllocationResponse allocation = fetchAllocation(studentId);

        if (allocation == null) {
            throw new BusinessException("Student is not allocated to any hostel");
        }

        validateLeaveDates(request.startDate(), request.endDate());

        /*
         * Prevent a student from stacking multiple overlapping
         * active leave requests.
         */
        validateNoOverlap(studentId, request.startDate(), request.endDate(), null);

        Leave leave = leaveMapper.toEntity(request);

        leave.setStudentId(studentId);
        leave.setHostelId(allocation.hostelId());
        leave.setRoomId(allocation.roomId());
        leave.setBedId(allocation.bedId());
        leave.setStatus(LeaveStatus.PENDING);
        leave.setHandledBy(null);
        leave.setClosedBy(null);
        leave.setDecisionAt(null);
        leave.setRejectionReason(null);

        Leave saved = leaveRepository.save(leave);

        return leaveMapper.toResponse(saved);
    }

    // =========================================================
    // GET SINGLE LEAVE
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public LeaveResponse getLeave(UUID leaveId) {
        return leaveMapper.toResponse(findLeave(leaveId));
    }

    // =========================================================
    // STUDENT - MY LEAVES
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getMyLeaves(UUID studentId) {
        return leaveRepository.findByStudentId(studentId)
                .stream()
                .map(leaveMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getStudentLeaves(UUID studentId) {
        return getMyLeaves(studentId);
    }

    // =========================================================
    // HOSTEL LEAVES
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getHostelLeaves(UUID hostelId) {
        return leaveRepository.findByHostelId(hostelId)
                .stream()
                .map(leaveMapper::toResponse)
                .toList();
    }

    // =========================================================
    // ALL LEAVES
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getAllLeaves() {
        return leaveRepository.findAll()
                .stream()
                .map(leaveMapper::toResponse)
                .toList();
    }

    // =========================================================
    // LEAVES BY STATUS
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getLeavesByStatus(String status) {

        if (status == null || status.isBlank()) {
            throw new BusinessException("Leave status is required");
        }

        LeaveStatus leaveStatus;
        try {
            leaveStatus = LeaveStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("Invalid leave status: " + status);
        }

        return leaveRepository.findByStatus(leaveStatus)
                .stream()
                .map(leaveMapper::toResponse)
                .toList();
    }

    // =========================================================
    // STUDENT - UPDATE LEAVE
    // =========================================================

    @Override
    public LeaveResponse updateLeave(UUID leaveId, UUID studentId, UpdateLeaveRequest request) {

        Leave leave = findLeave(leaveId);
        validateOwnership(leave, studentId);

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException("Only pending leave requests can be updated");
        }

        validateLeaveDates(request.startDate(), request.endDate());
        validateNoOverlap(studentId, request.startDate(), request.endDate(), leaveId);

        leave.setStartDate(request.startDate());
        leave.setEndDate(request.endDate());
        leave.setReason(request.reason().trim());

        return leaveMapper.toResponse(leaveRepository.save(leave));
    }

    // =========================================================
    // WARDEN - ACCEPT LEAVE
    // =========================================================

    @Override
    public LeaveResponse acceptLeave(UUID leaveId, UUID wardenId) {

        Leave leave = findLeave(leaveId);

        UserResponse warden = fetchUser(wardenId);
        validateWarden(warden);
        validateSameHostel(warden, leave.getHostelId());

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException("Only pending leave requests can be accepted");
        }

        if (leave.getHandledBy() != null) {
            throw new BusinessException("Leave request is already being handled");
        }

        leave.setHandledBy(wardenId);
        leave.setStatus(LeaveStatus.IN_REVIEW);

        try {
            /*
             * @Version on Leave makes this save atomic against
             * concurrent accepts: if another warden's save wins
             * the race, this one throws instead of silently
             * clobbering the first assignment.
             */
            Leave saved = leaveRepository.saveAndFlush(leave);
            return leaveMapper.toResponse(saved);
        } catch (OptimisticLockingFailureException ex) {
            throw new BusinessException(
                    "Leave request was just accepted by another warden"
            );
        }
    }

    // =========================================================
    // WARDEN - APPROVE / REJECT
    // =========================================================

    @Override
    public LeaveResponse updateLeaveStatus(UUID leaveId, UUID wardenId, UpdateLeaveStatusRequest request) {

        Leave leave = findLeave(leaveId);

        if (leave.getHandledBy() == null) {
            throw new BusinessException("Accept the leave request first");
        }

        if (!leave.getHandledBy().equals(wardenId)) {
            throw new BusinessException("Only the assigned warden can update this leave request");
        }

        UserResponse warden = fetchUser(wardenId);
        validateWarden(warden);
        validateSameHostel(warden, leave.getHostelId());

        LeaveStatus newStatus = request.status();
        validateStatusChange(leave.getStatus(), newStatus);

        if (newStatus == LeaveStatus.REJECTED) {
            if (request.remarks() == null || request.remarks().isBlank()) {
                throw new BusinessException("Rejection reason is required");
            }
            leave.setRejectionReason(request.remarks().trim());
        }

        if (request.remarks() != null && !request.remarks().isBlank()) {
            leave.setRemarks(request.remarks().trim());
        }

        leave.setStatus(newStatus);

        if (newStatus == LeaveStatus.APPROVED || newStatus == LeaveStatus.REJECTED) {
            leave.setClosedBy(wardenId);
            leave.setDecisionAt(LocalDateTime.now());
        }

        try {
            return leaveMapper.toResponse(leaveRepository.saveAndFlush(leave));
        } catch (OptimisticLockingFailureException ex) {
            throw new BusinessException(
                    "Leave request was modified concurrently, please retry"
            );
        }
    }

    // =========================================================
    // STUDENT - DELETE / CANCEL LEAVE
    // =========================================================

    @Override
    public void deleteLeave(UUID leaveId, UUID studentId) {

        Leave leave = findLeave(leaveId);
        validateOwnership(leave, studentId);

        if (leave.getStatus() != LeaveStatus.PENDING) {
            throw new BusinessException("Only pending leave requests can be cancelled");
        }

        leave.setStatus(LeaveStatus.CANCELLED);
        leaveRepository.save(leave);
    }

    // =========================================================
    // FIND LEAVE
    // =========================================================

    private Leave findLeave(UUID leaveId) {
        return leaveRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Leave request not found with id: " + leaveId));
    }

    // =========================================================
    // DOWNSTREAM CALLS (translate Feign errors cleanly)
    // =========================================================

    private UserResponse fetchUser(UUID userId) {
        try {
            return identityClient.getUserById(userId);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        } catch (FeignException ex) {
            throw new BusinessException("Unable to reach identity-service");
        }
    }

    private StudentAllocationResponse fetchAllocation(UUID studentId) {
        try {
            return allocationClient.getStudentAllocation(studentId);
        } catch (FeignException.NotFound ex) {
            return null;
        } catch (FeignException ex) {
            throw new BusinessException("Unable to reach allocation-service");
        }
    }

    // =========================================================
    // VALIDATE STUDENT
    // =========================================================

    private void validateStudent(UserResponse user) {

        if (user == null) {
            throw new ResourceNotFoundException("Student not found");
        }

        if (user.roles() == null || !user.roles().contains("STUDENT")) {
            throw new BusinessException("Only students can apply for leave");
        }

        if (!Boolean.TRUE.equals(user.active())) {
            throw new BusinessException("Student account is inactive");
        }
    }

    // =========================================================
    // VALIDATE WARDEN
    // =========================================================

    private void validateWarden(UserResponse user) {

        if (user == null) {
            throw new ResourceNotFoundException("Warden not found");
        }

        if (user.roles() == null || !user.roles().contains("WARDEN")) {
            throw new BusinessException("Only wardens can manage leave requests");
        }

        if (!Boolean.TRUE.equals(user.active())) {
            throw new BusinessException("Warden account is inactive");
        }

        if (user.hostelId() == null) {
            throw new BusinessException("Warden is not assigned to any hostel");
        }
    }

    // =========================================================
    // VALIDATE SAME HOSTEL
    // =========================================================

    private void validateSameHostel(UserResponse warden, UUID hostelId) {
        if (!warden.hostelId().equals(hostelId)) {
            throw new BusinessException("You cannot manage leave requests of another hostel");
        }
    }

    // =========================================================
    // VALIDATE OWNERSHIP
    // =========================================================

    private void validateOwnership(Leave leave, UUID studentId) {
        if (!leave.getStudentId().equals(studentId)) {
            throw new BusinessException("You are not allowed to modify this leave request");
        }
    }

    // =========================================================
    // VALIDATE DATES
    // =========================================================

    private void validateLeaveDates(LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new BusinessException("Start date and end date are required");
        }

        if (startDate.isAfter(endDate)) {
            throw new BusinessException("Start date cannot be after end date");
        }

        if (startDate.isBefore(LocalDate.now())) {
            throw new BusinessException("Leave cannot start in the past");
        }
    }

    // =========================================================
    // VALIDATE NO OVERLAP
    // =========================================================

    private void validateNoOverlap(UUID studentId, LocalDate startDate, LocalDate endDate, UUID excludeLeaveId) {

        boolean overlaps = leaveRepository.findByStudentId(studentId).stream()
                .filter(l -> excludeLeaveId == null || !l.getId().equals(excludeLeaveId))
                .filter(l -> l.getStatus() == LeaveStatus.PENDING || l.getStatus() == LeaveStatus.IN_REVIEW)
                .anyMatch(l -> !startDate.isAfter(l.getEndDate()) && !endDate.isBefore(l.getStartDate()));

        if (overlaps) {
            throw new BusinessException(
                    "You already have an active leave request overlapping these dates"
            );
        }
    }

    // =========================================================
    // VALIDATE STATUS TRANSITION
    // =========================================================

    private void validateStatusChange(LeaveStatus currentStatus, LeaveStatus newStatus) {

        if (newStatus == null) {
            throw new BusinessException("Leave status is required");
        }

        if (currentStatus == LeaveStatus.APPROVED ||
                currentStatus == LeaveStatus.REJECTED ||
                currentStatus == LeaveStatus.CANCELLED) {
            throw new BusinessException("Leave request is already closed");
        }

        if (newStatus == LeaveStatus.PENDING) {
            throw new BusinessException("Cannot move leave request back to PENDING");
        }

        if (currentStatus == LeaveStatus.PENDING) {
            throw new BusinessException("Leave request must first be accepted by a warden");
        }

        if (currentStatus == LeaveStatus.IN_REVIEW &&
                newStatus != LeaveStatus.APPROVED &&
                newStatus != LeaveStatus.REJECTED) {
            throw new BusinessException("Leave in review can only be approved or rejected");
        }
    }
}