package com.hostelhub.complaintservice.mapper;

import com.hostelhub.complaintservice.dto.request.CreateComplaintRequest;
import com.hostelhub.complaintservice.dto.response.ComplaintResponse;
import com.hostelhub.complaintservice.entity.Complaint;
import org.springframework.stereotype.Component;

@Component
public class ComplaintMapper {

    public Complaint toEntity(
            CreateComplaintRequest request
    ) {

        return Complaint.builder()
                .category(request.category())
                .title(request.title().trim())
                .description(request.description().trim())
                .priority(
                        request.priority() != null
                                ? request.priority()
                                : com.hostelhub.complaintservice.entity.ComplaintPriority.MEDIUM
                )
                .build();
    }

    public ComplaintResponse toResponse(
            Complaint complaint
    ) {

        return ComplaintResponse.builder()
                .id(complaint.getId())
                .studentId(complaint.getStudentId())
                .category(complaint.getCategory())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .priority(complaint.getPriority())
                .status(complaint.getStatus())
                .hostelId(complaint.getHostelId())
                .roomId(complaint.getRoomId())
                .bedId(complaint.getBedId())
                .assignedWardenId(complaint.getAssignedWardenId())
                .assignedWardenRemarks(complaint.getAssignedWardenRemarks())
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .resolvedAt(complaint.getResolvedAt())
                .build();
    }
}