package com.hostelhub.complaintservice.service;

import com.hostelhub.complaintservice.dto.request.CreateComplaintRequest;
import com.hostelhub.complaintservice.dto.request.UpdateComplaintRequest;
import com.hostelhub.complaintservice.dto.request.UpdateComplaintStatusRequest;
import com.hostelhub.complaintservice.dto.response.ComplaintResponse;

import java.util.List;
import java.util.UUID;

public interface ComplaintService {

    ComplaintResponse createComplaint(
            UUID studentId,
            CreateComplaintRequest request
    );

    ComplaintResponse getComplaint(
            UUID complaintId
    );

    List<ComplaintResponse> getMyComplaints(
            UUID studentId
    );

    List<ComplaintResponse> getAllComplaints();

    List<ComplaintResponse> getComplaintsByStatus(
            String status
    );

    ComplaintResponse updateComplaint(
            UUID complaintId,
            UUID studentId,
            UpdateComplaintRequest request
    );

    ComplaintResponse updateComplaintStatus(
            UUID complaintId,
            UUID wardenId,
            UpdateComplaintStatusRequest request
    );

    void deleteComplaint(
            UUID complaintId,
            UUID studentId
    );

    ComplaintResponse acceptComplaint(
            UUID complaintId,
            UUID wardenId
    );


    List<ComplaintResponse> getHostelComplaints(
            UUID hostelId
    );

    List<ComplaintResponse> getStudentComplaints(
            UUID studentId
    );
}