package com.hostelhub.hostelservice.mapper;

import com.hostelhub.hostelservice.dto.request.CreateHostelRequest;
import com.hostelhub.hostelservice.dto.request.UpdateHostelRequest;
import com.hostelhub.hostelservice.dto.response.HostelResponse;
import com.hostelhub.hostelservice.entity.Hostel;
import org.springframework.stereotype.Component;

@Component
public class HostelMapper {

    public Hostel toEntity(CreateHostelRequest request) {

        return Hostel.builder()
                .name(request.name().trim())
                .description(request.description())
                .type(request.type())
                .address(request.address().trim())
                .city(request.city().trim())
                .state(request.state().trim())
                .pincode(request.pincode().trim())
                .totalFloors(request.totalFloors())
                .adminId(request.adminId())
                .build();
    }

    public void updateEntity(
            Hostel hostel,
            UpdateHostelRequest request
    ) {

        hostel.setName(request.name().trim());
        hostel.setDescription(request.description());
        hostel.setType(request.type());
        hostel.setStatus(request.status());
        hostel.setAddress(request.address().trim());
        hostel.setCity(request.city().trim());
        hostel.setState(request.state().trim());
        hostel.setPincode(request.pincode().trim());
        hostel.setTotalFloors(request.totalFloors());
        hostel.setAdminId(request.adminId());
    }

    public HostelResponse toResponse(Hostel hostel) {

        return new HostelResponse(
                hostel.getId(),
                hostel.getName(),
                hostel.getDescription(),
                hostel.getType(),
                hostel.getStatus(),
                hostel.getAddress(),
                hostel.getCity(),
                hostel.getState(),
                hostel.getPincode(),
                hostel.getTotalFloors(),
                hostel.getCreatedAt(),
                hostel.getUpdatedAt(),
                hostel.getAdminId()

        );
    }
}