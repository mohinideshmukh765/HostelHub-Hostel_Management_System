package com.hostelhub.leaveservice.repository;

import com.hostelhub.leaveservice.entity.Leave;
import com.hostelhub.leaveservice.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeaveRepository extends JpaRepository<Leave, UUID> {

    List<Leave> findByStudentId(UUID studentId);

    List<Leave> findByHostelId(UUID hostelId);

    List<Leave> findByStatus(LeaveStatus status);

    List<Leave> findByHostelIdAndStatus(
            UUID hostelId,
            LeaveStatus status
    );

    List<Leave> findByHandledBy(UUID handledBy);

}