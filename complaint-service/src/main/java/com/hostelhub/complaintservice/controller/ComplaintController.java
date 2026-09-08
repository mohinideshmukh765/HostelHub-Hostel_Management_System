package com.hostelhub.complaintservice.controller;

import com.hostelhub.complaintservice.dto.request.CreateComplaintRequest;
import com.hostelhub.complaintservice.dto.request.UpdateComplaintRequest;
import com.hostelhub.complaintservice.dto.request.UpdateComplaintStatusRequest;
import com.hostelhub.complaintservice.dto.response.ComplaintResponse;
import com.hostelhub.complaintservice.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
public class ComplaintController {

    private final ComplaintService complaintService;

    // Student
    @PostMapping
    public ResponseEntity<ComplaintResponse> createComplaint(
            @RequestParam UUID studentId,
            @Valid @RequestBody CreateComplaintRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        complaintService.createComplaint(
                                studentId,
                                request
                        )
                );
    }

    // Student / Warden / Admin
    @GetMapping("/{complaintId}")
    public ResponseEntity<ComplaintResponse> getComplaint(
            @PathVariable UUID complaintId
    ) {

        return ResponseEntity.ok(
                complaintService.getComplaint(complaintId)
        );
    }

    // Student
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ComplaintResponse>> getMyComplaints(
            @PathVariable UUID studentId
    ) {

        return ResponseEntity.ok(
                complaintService.getMyComplaints(studentId)
        );
    }

    // Warden / Admin
    @GetMapping
    public ResponseEntity<List<ComplaintResponse>> getAllComplaints(
            @RequestParam(required = false) String status
    ) {

        if (status != null && !status.isBlank()) {

            return ResponseEntity.ok(
                    complaintService.getComplaintsByStatus(status)
            );
        }

        return ResponseEntity.ok(
                complaintService.getAllComplaints()
        );
    }

    // Student
    @PutMapping("/{complaintId}")
    public ResponseEntity<ComplaintResponse> updateComplaint(
            @PathVariable UUID complaintId,

            @RequestParam UUID studentId,

            @Valid @RequestBody UpdateComplaintRequest request
    ) {

        return ResponseEntity.ok(
                complaintService.updateComplaint(
                        complaintId,
                        studentId,
                        request
                )
        );
    }

    // Warden
    @PutMapping("/{complaintId}/accept")
    public ResponseEntity<ComplaintResponse> acceptComplaint(
            @PathVariable UUID complaintId,
            @RequestParam UUID wardenId
    ) {
        return ResponseEntity.ok(
                complaintService.acceptComplaint(
                        complaintId,
                        wardenId
                )
        );
    }

    // Warden
    @PutMapping("/{complaintId}/status")
    public ResponseEntity<ComplaintResponse> updateComplaintStatus(
            @PathVariable UUID complaintId,

            @RequestParam UUID wardenId,

            @Valid @RequestBody UpdateComplaintStatusRequest request
    ) {

        return ResponseEntity.ok(
                complaintService.updateComplaintStatus(
                        complaintId,
                        wardenId,
                        request
                )
        );
    }

    // Student
    @DeleteMapping("/{complaintId}")
    public ResponseEntity<Void> deleteComplaint(
            @PathVariable UUID complaintId,

            @RequestParam UUID studentId
    ) {

        complaintService.deleteComplaint(
                complaintId,
                studentId
        );

        return ResponseEntity.noContent().build();
    }
}