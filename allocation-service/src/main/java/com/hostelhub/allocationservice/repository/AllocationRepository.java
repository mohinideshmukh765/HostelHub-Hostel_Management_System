package com.hostelhub.allocationservice.repository;

import com.hostelhub.allocationservice.entity.Allocation;
import com.hostelhub.allocationservice.entity.AllocationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AllocationRepository
        extends JpaRepository<Allocation, UUID> {

    Optional<Allocation> findByStudentIdAndStatus(
            UUID studentId,
            AllocationStatus status
    );

    Optional<Allocation> findByBedIdAndStatus(
            UUID bedId,
            AllocationStatus status
    );

    List<Allocation> findByStudentId(
            UUID studentId
    );

    List<Allocation> findByStatus(
            AllocationStatus status
    );

    boolean existsByStudentIdAndStatus(
            UUID studentId,
            AllocationStatus status
    );

    boolean existsByBedIdAndStatus(
            UUID bedId,
            AllocationStatus status
    );



}