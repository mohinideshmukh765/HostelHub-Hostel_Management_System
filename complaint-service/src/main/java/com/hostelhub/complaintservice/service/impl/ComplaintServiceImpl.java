package com.hostelhub.complaintservice.service.impl;

import com.hostelhub.complaintservice.client.AllocationClient;
import com.hostelhub.complaintservice.client.IdentityClient;
import com.hostelhub.complaintservice.dto.request.CreateComplaintRequest;
import com.hostelhub.complaintservice.dto.request.UpdateComplaintRequest;
import com.hostelhub.complaintservice.dto.request.UpdateComplaintStatusRequest;
import com.hostelhub.complaintservice.dto.response.ComplaintResponse;
import com.hostelhub.complaintservice.dto.response.StudentAllocationResponse;
import com.hostelhub.complaintservice.dto.response.UserResponse;
import com.hostelhub.complaintservice.entity.Complaint;
import com.hostelhub.complaintservice.entity.ComplaintStatus;
import com.hostelhub.complaintservice.exception.BusinessException;
import com.hostelhub.complaintservice.exception.ResourceNotFoundException;
import com.hostelhub.complaintservice.mapper.ComplaintMapper;
import com.hostelhub.complaintservice.repository.ComplaintRepository;
import com.hostelhub.complaintservice.service.ComplaintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ComplaintServiceImpl
        implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintMapper complaintMapper;
    private final IdentityClient identityClient;
    private final AllocationClient allocationClient;

    @Override
    public ComplaintResponse createComplaint(
            UUID studentId,
            CreateComplaintRequest request
    ) {

        UserResponse student =
                identityClient.getUserById(studentId);


        validateStudent(student);

        StudentAllocationResponse allocation =
                allocationClient.getStudentAllocation(studentId);

        if (allocation == null) {
            throw new BusinessException(
                    "Student is not allocated to any hostel"
            );
        }

        Complaint complaint =
                complaintMapper.toEntity(request);

        complaint.setStudentId(studentId);

        complaint.setHostelId(allocation.hostelId());

        complaint.setRoomId(allocation.roomId());

        complaint.setBedId(allocation.bedId());

        complaint.setStatus(ComplaintStatus.OPEN);

        complaint.setHandledBy(null);

        Complaint saved = complaintRepository.save(complaint);

        return complaintMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaint(
            UUID complaintId
    ) {

        Complaint complaint =
                findComplaint(complaintId);

        return complaintMapper.toResponse(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getMyComplaints(
            UUID studentId
    ) {

        return complaintRepository
                .findByStudentId(studentId)
                .stream()
                .map(complaintMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getAllComplaints() {

        return complaintRepository.findAll()
                .stream()
                .map(complaintMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getComplaintsByStatus(
            String status
    ) {

        ComplaintStatus complaintStatus;

        try {

            complaintStatus =
                    ComplaintStatus.valueOf(
                            status.toUpperCase()
                    );

        } catch (IllegalArgumentException ex) {

            throw new BusinessException(
                    "Invalid complaint status: " + status
            );
        }

        return complaintRepository
                .findByStatus(complaintStatus)
                .stream()
                .map(complaintMapper::toResponse)
                .toList();
    }

    @Override
    public ComplaintResponse updateComplaint(
            UUID complaintId,
            UUID studentId,
            UpdateComplaintRequest request
    ) {

        Complaint complaint =
                findComplaint(complaintId);

        validateOwnership(
                complaint,
                studentId
        );

        if (complaint.getStatus() != ComplaintStatus.OPEN) {

            throw new BusinessException(
                    "Only OPEN complaints can be updated"
            );
        }

        complaint.setTitle(
                request.title().trim()
        );

        complaint.setDescription(
                request.description().trim()
        );

        if (request.priority() != null) {

            complaint.setPriority(
                    request.priority()
            );
        }

        return complaintMapper.toResponse(
                complaintRepository.save(complaint)
        );
    }

    @Override
    public ComplaintResponse updateComplaintStatus(
            UUID complaintId,
            UUID loggedInWardenId,
            UpdateComplaintStatusRequest request
    ) {

        Complaint complaint = findComplaint(complaintId);

        if (complaint.getHandledBy() == null) {

            throw new BusinessException(
                    "Accept the complaint first."
            );
        }

        if (!complaint.getHandledBy().equals(loggedInWardenId)) {

            throw new BusinessException(
                    "Only the assigned warden can update this complaint."
            );
        }

        UserResponse warden =
                identityClient.getUserById(loggedInWardenId);

        validateWarden(warden);

        if (!warden.hostelId().equals(complaint.getHostelId())) {

            throw new BusinessException(
                    "You cannot update complaints of another hostel."
            );
        }

        ComplaintStatus newStatus =
                request.status();

        validateStatusChange(
                complaint.getStatus(),
                newStatus
        );

        complaint.setStatus(newStatus);

        if (request.remarks() != null &&
                !request.remarks().isBlank()) {

            complaint.setResolutionRemarks(
                    request.remarks().trim()
            );
        }

        if (newStatus == ComplaintStatus.RESOLVED) {

            complaint.setResolvedAt(
                    LocalDateTime.now()
            );

        } else {

            complaint.setResolvedAt(null);
        }

        return complaintMapper.toResponse(
                complaintRepository.save(complaint)
        );
    }

    @Override
    public void deleteComplaint(
            UUID complaintId,
            UUID studentId
    ) {

        Complaint complaint =
                findComplaint(complaintId);

        validateOwnership(
                complaint,
                studentId
        );

        if (complaint.getStatus() != ComplaintStatus.OPEN) {

            throw new BusinessException(
                    "Only OPEN complaints can be deleted"
            );
        }

        complaintRepository.delete(complaint);
    }

    @Override
    public ComplaintResponse acceptComplaint(
            UUID complaintId,
            UUID wardenId
    ) {

        Complaint complaint =
                findComplaint(complaintId);

        UserResponse warden =
                identityClient.getUserById(wardenId);

        validateWarden(warden);

        if (!warden.hostelId().equals(complaint.getHostelId())) {

            throw new BusinessException(
                    "You cannot accept complaints from another hostel."
            );
        }

        if (complaint.getHandledBy() != null) {

            throw new BusinessException(
                    "Complaint already accepted by another warden"
            );
        }

        complaint.setHandledBy(wardenId);

        complaint.setStatus(
                ComplaintStatus.IN_PROGRESS
        );

        Complaint saved =
                complaintRepository.save(complaint);

        return complaintMapper.toResponse(saved);
    }

    @Override
    public List<ComplaintResponse> getHostelComplaints(UUID hostelId) {
        return complaintRepository
                .findByHostelId(hostelId)
                .stream()
                .map(complaintMapper::toResponse)
                .toList();
    }

    @Override
    public List<ComplaintResponse> getStudentComplaints(
            UUID studentId
    ) {

        return complaintRepository
                .findByStudentId(studentId)
                .stream()
                .map(complaintMapper::toResponse)
                .toList();
    }

    private Complaint findComplaint(
            UUID complaintId
    ) {

        return complaintRepository
                .findById(complaintId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Complaint not found with id: "
                                        + complaintId
                        )
                );
    }

    private void validateOwnership(
            Complaint complaint,
            UUID studentId
    ) {

        if (!complaint.getStudentId().equals(studentId)) {

            throw new BusinessException(
                    "You are not allowed to modify this complaint"
            );
        }
    }

    private void validateStudent(UserResponse user) {

        if (user == null) {
            throw new ResourceNotFoundException("Student not found");
        }

        if (user.roles() == null ||
                !user.roles().contains("STUDENT")) {

            throw new BusinessException(
                    "Only students can create complaints"
            );
        }

        if (!user.active()) {
            throw new BusinessException(
                    "Student account is not active"
            );
        }

    }

    private void validateWarden(
            UserResponse user
    ) {

        if (user == null) {

            throw new ResourceNotFoundException(
                    "Warden not found"
            );
        }

        if (user.roles() == null ||
                !user.roles().contains("WARDEN")) {

            throw new BusinessException(
                    "Only wardens can update complaint status"
            );
        }

        if (!user.active()) {
            throw new BusinessException(
                    "Warden account is not active"
            );
        }
    }

    private void validateStatusChange(
            ComplaintStatus currentStatus,
            ComplaintStatus newStatus
    ) {

        if (currentStatus == ComplaintStatus.RESOLVED ||
                currentStatus == ComplaintStatus.REJECTED) {

            throw new BusinessException(
                    "Resolved or rejected complaints cannot be modified"
            );
        }

        if (newStatus == ComplaintStatus.OPEN) {

            throw new BusinessException(
                    "Complaint cannot be moved back to OPEN"
            );
        }

        if (currentStatus == ComplaintStatus.OPEN &&
                newStatus != ComplaintStatus.IN_PROGRESS &&
                newStatus != ComplaintStatus.REJECTED) {

            throw new BusinessException(
                    "OPEN complaint can only be moved to IN_PROGRESS or REJECTED"
            );
        }

        if (currentStatus == ComplaintStatus.IN_PROGRESS &&
                newStatus != ComplaintStatus.RESOLVED &&
                newStatus != ComplaintStatus.REJECTED) {

            throw new BusinessException(
                    "IN_PROGRESS complaint can only be RESOLVED or REJECTED"
            );
        }
    }
}