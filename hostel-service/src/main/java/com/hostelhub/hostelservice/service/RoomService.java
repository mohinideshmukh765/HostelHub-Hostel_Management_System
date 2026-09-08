package com.hostelhub.hostelservice.service;

import com.hostelhub.hostelservice.dto.request.CreateRoomRequest;
import com.hostelhub.hostelservice.dto.request.UpdateRoomRequest;
import com.hostelhub.hostelservice.dto.response.RoomResponse;
import com.hostelhub.hostelservice.entity.HostelStatus;
import com.hostelhub.hostelservice.entity.RoomType;

import java.util.List;
import java.util.UUID;

public interface RoomService {

    RoomResponse createRoom(
            UUID hostelId,
            CreateRoomRequest request
    );

    RoomResponse getRoom(
            UUID hostelId,
            UUID roomId
    );

    List<RoomResponse> searchRooms(
            UUID hostelId,
            HostelStatus status,
            RoomType type,
            Integer floor
    );

    RoomResponse updateRoom(
            UUID hostelId,
            UUID roomId,
            UpdateRoomRequest request
    );

    void deleteRoom(
            UUID hostelId,
            UUID roomId
    );
}