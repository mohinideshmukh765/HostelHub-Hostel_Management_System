package com.hostelhub.hostelservice.mapper;

import com.hostelhub.hostelservice.dto.request.CreateRoomRequest;
import com.hostelhub.hostelservice.dto.request.UpdateRoomRequest;
import com.hostelhub.hostelservice.dto.response.RoomResponse;
import com.hostelhub.hostelservice.entity.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public Room toEntity(CreateRoomRequest request) {

        return Room.builder()
                .roomNumber(request.roomNumber().trim())
                .floor(request.floor())
                .type(request.type())
                .capacity(request.capacity())
                .build();
    }

    public void updateEntity(
            Room room,
            UpdateRoomRequest request
    ) {

        room.setRoomNumber(request.roomNumber().trim());
        room.setFloor(request.floor());
        room.setType(request.type());
        room.setCapacity(request.capacity());
        room.setStatus(request.status());
    }

    public RoomResponse toResponse(Room room) {

        return new RoomResponse(
                room.getId(),
                room.getHostel().getId(),
                room.getRoomNumber(),
                room.getFloor(),
                room.getType(),
                room.getCapacity(),
                room.getStatus(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }
}