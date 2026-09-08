package com.hostelhub.allocationservice.service.impl;

import com.hostelhub.allocationservice.client.HostelClient;
import com.hostelhub.allocationservice.client.IdentityClient;
import com.hostelhub.allocationservice.dto.external.BedResponse;
import com.hostelhub.allocationservice.dto.external.StudentResponse;
import com.hostelhub.allocationservice.dto.request.CheckoutRequest;
import com.hostelhub.allocationservice.dto.request.CreateAllocationRequest;
import com.hostelhub.allocationservice.dto.response.AllocationResponse;
import com.hostelhub.allocationservice.dto.response.StudentAllocationResponse;
import com.hostelhub.allocationservice.entity.Allocation;
import com.hostelhub.allocationservice.entity.AllocationStatus;
import com.hostelhub.allocationservice.exception.BusinessException;
import com.hostelhub.allocationservice.exception.ResourceNotFoundException;
import com.hostelhub.allocationservice.mapper.AllocationMapper;
import com.hostelhub.allocationservice.repository.AllocationRepository;
import com.hostelhub.allocationservice.service.AllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AllocationServiceImpl implements AllocationService {

    private final AllocationRepository allocationRepository;
    private final AllocationMapper allocationMapper;

    private final IdentityClient identityClient;
    private final HostelClient hostelClient;

    @Override
    @Transactional
    public AllocationResponse createAllocation(
            CreateAllocationRequest request
    ) {

        // 1. Check student
        StudentResponse student =
                identityClient.getStudent(request.studentId());

        if (!student.active()) {
            throw new BusinessException(
                    "Student account is not active"
            );
        }

        boolean isStudent =
                student.roles()
                        .stream()
                        .anyMatch(role ->
                                role.equals("STUDENT")
                        );

        if (!isStudent) {
            throw new BusinessException(
                    "User is not a student"
            );
        }

        // 2. Check existing active allocation
        if (allocationRepository
                .existsByStudentIdAndStatus(
                        request.studentId(),
                        AllocationStatus.ACTIVE
                )) {

            throw new BusinessException(
                    "Student already has an active allocation"
            );
        }

        // 3. Check bed
        BedResponse bed =
                hostelClient.getBed(request.bedId());

        if (bed.status() !=
                com.hostelhub.allocationservice.dto.external.BedStatus.AVAILABLE) {

            throw new BusinessException(
                    "Bed is not available"
            );
        }

        // 4. Check if bed already allocated
        if (allocationRepository
                .existsByBedIdAndStatus(
                        request.bedId(),
                        AllocationStatus.ACTIVE
                )) {

            throw new BusinessException(
                    "Bed is already allocated"
            );
        }

        // 5. Occupy bed
        hostelClient.occupyBed(
                request.bedId()
        );

        Allocation allocation = Allocation.builder()

                .studentId(request.studentId())

                .hostelId(bed.hostelId())

                .bedId(request.bedId())

                .allocatedDate(LocalDate.now())

                .status(AllocationStatus.ACTIVE)

                .remarks(request.remarks())

                .build();

        try {
            Allocation saved = allocationRepository.save(allocation);
            return allocationMapper.toResponse(saved);
        } catch (OptimisticLockingFailureException ex) {
            // Two concurrent requests both passed the bed-availability check.
            // Roll back bed occupation and surface a clear error.
            hostelClient.releaseBed(request.bedId());
            throw new BusinessException(
                    "Bed was just allocated by another request — please try again"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AllocationResponse getAllocation(
            UUID id
    ) {

        Allocation allocation =
                allocationRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Allocation not found"
                                )
                        );

        return allocationMapper.toResponse(
                allocation
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AllocationResponse> getStudentAllocations(
            UUID studentId
    ) {

        return allocationRepository
                .findByStudentId(studentId)
                .stream()
                .map(allocationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public AllocationResponse checkout(
            UUID id,
            CheckoutRequest request
    ) {

        Allocation allocation =
                allocationRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Allocation not found"
                                )
                        );

        if (allocation.getStatus() !=
                AllocationStatus.ACTIVE) {

            throw new BusinessException(
                    "Allocation is not active"
            );
        }

        // Release bed
        hostelClient.releaseBed(
                allocation.getBedId()
        );

        // Complete allocation
        allocation.setStatus(
                AllocationStatus.COMPLETED
        );

        allocation.setCheckoutDate(
                LocalDate.now()
        );

        if (request != null &&
                request.remarks() != null &&
                !request.remarks().isBlank()) {

            allocation.setRemarks(
                    request.remarks()
            );
        }

        Allocation updated =
                allocationRepository.save(
                        allocation
                );

        return allocationMapper.toResponse(
                updated
        );
    }

    @Override
    @Transactional(readOnly = true)
    public StudentAllocationResponse getStudentAllocation(UUID studentId) {

        Allocation allocation = allocationRepository
                .findByStudentIdAndStatus(
                        studentId,
                        AllocationStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "No active allocation found"
                        ));

        BedResponse bed =
                hostelClient.getBed(
                        allocation.getBedId()
                );

        return new StudentAllocationResponse(

                allocation.getStudentId(),

                allocation.getHostelId(),

                bed.roomId(),

                allocation.getBedId()

        );
    }
}