package com.hostelhub.hostelservice.mapper;

import com.hostelhub.hostelservice.dto.request.CreateBedRequest;
import com.hostelhub.hostelservice.dto.request.UpdateBedRequest;
import com.hostelhub.hostelservice.dto.response.BedResponse;
import com.hostelhub.hostelservice.entity.Bed;
import org.springframework.stereotype.Component;

@Component
public class BedMapper {

    public Bed toEntity(CreateBedRequest request) {

        return Bed.builder()
                .bedNumber(request.bedNumber().trim())
                .build();
    }

    public void updateEntity(
            Bed bed,
            UpdateBedRequest request
    ) {

        bed.setBedNumber(request.bedNumber().trim());
        bed.setStatus(request.status());
    }

    public BedResponse toResponse(Bed bed) {

        return new BedResponse(

                bed.getId(),

                bed.getRoom().getHostel().getId(),

                bed.getRoom().getId(),


                bed.getBedNumber(),

                bed.getStatus(),

                bed.getCreatedAt(),

                bed.getUpdatedAt()

        );
    }
}