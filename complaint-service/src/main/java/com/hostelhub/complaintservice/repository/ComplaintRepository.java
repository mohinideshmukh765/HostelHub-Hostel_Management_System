package com.hostelhub.complaintservice.repository;

import com.hostelhub.complaintservice.entity.Complaint;
import com.hostelhub.complaintservice.entity.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ComplaintRepository
        extends JpaRepository<Complaint, UUID> {

    List<Complaint> findByStudentId(UUID studentId);

    List<Complaint> findByStatus(ComplaintStatus status);


    List<Complaint> findByStudentIdAndStatus(
            UUID studentId,
            ComplaintStatus status
    );

    List<Complaint> findByAssignedWardenId(
            UUID assignedWardenId
    );


    List<Complaint> findByHostelId(UUID hostelId);

    List<Complaint> findByHandledBy(UUID handledBy);

    List<Complaint> findByHostelIdAndStatus(
            UUID hostelId,
            ComplaintStatus status
    );

}