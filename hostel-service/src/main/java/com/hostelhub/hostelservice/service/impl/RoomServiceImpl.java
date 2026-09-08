package com.hostelhub.hostelservice.service.impl;

import com.hostelhub.hostelservice.dto.request.CreateRoomRequest;
import com.hostelhub.hostelservice.dto.request.UpdateRoomRequest;
import com.hostelhub.hostelservice.dto.response.RoomResponse;
import com.hostelhub.hostelservice.entity.Hostel;
import com.hostelhub.hostelservice.entity.HostelStatus;
import com.hostelhub.hostelservice.entity.Room;
import com.hostelhub.hostelservice.entity.RoomType;
import com.hostelhub.hostelservice.exception.ResourceNotFoundException;
import com.hostelhub.hostelservice.mapper.RoomMapper;
import com.hostelhub.hostelservice.repository.HostelRepository;
import com.hostelhub.hostelservice.repository.RoomRepository;
import com.hostelhub.hostelservice.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HostelRepository hostelRepository;
    private final RoomMapper roomMapper;

    @Override
    @Transactional
    public RoomResponse createRoom(
            UUID hostelId,
            CreateRoomRequest request
    ) {

        Hostel hostel = findHostel(hostelId);

        validateFloor(hostel, request.floor());

        validateCapacity(
                request.type(),
                request.capacity()
        );

        if (roomRepository.existsByHostelIdAndRoomNumber(
                hostelId,
                request.roomNumber().trim()
        )) {

            throw new IllegalArgumentException(
                    "Room already exists with number: "
                            + request.roomNumber()
            );
        }

        Room room = roomMapper.toEntity(request);

        room.setHostel(hostel);

        Room savedRoom = roomRepository.save(room);

        return roomMapper.toResponse(savedRoom);
    }

    @Override
    public RoomResponse getRoom(
            UUID hostelId,
            UUID roomId
    ) {

        Room room = findRoom(hostelId, roomId);

        return roomMapper.toResponse(room);
    }

    @Override
    public List<RoomResponse> searchRooms(
            UUID hostelId,
            HostelStatus status,
            RoomType type,
            Integer floor
    ) {

        // Verify hostel exists
        findHostel(hostelId);

        List<Room> rooms;

        if (status != null) {

            rooms = roomRepository
                    .findByHostelIdAndStatus(
                            hostelId,
                            status
                    );

        } else if (floor != null) {

            rooms = roomRepository
                    .findByHostelIdAndFloor(
                            hostelId,
                            floor
                    );

        } else if (type != null) {

            rooms = roomRepository
                    .findByHostelIdAndType(
                            hostelId,
                            type
                    );

        } else {

            rooms = roomRepository
                    .findByHostelId(hostelId);
        }

        return rooms.stream()
                .map(roomMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(
            UUID hostelId,
            UUID roomId,
            UpdateRoomRequest request
    ) {

        Hostel hostel = findHostel(hostelId);

        Room room = findRoom(hostelId, roomId);

        validateFloor(hostel, request.floor());

        validateCapacity(
                request.type(),
                request.capacity()
        );

        boolean roomNumberChanged =
                !room.getRoomNumber()
                        .equalsIgnoreCase(
                                request.roomNumber().trim()
                        );

        if (roomNumberChanged &&
                roomRepository.existsByHostelIdAndRoomNumber(
                        hostelId,
                        request.roomNumber().trim()
                )) {

            throw new IllegalArgumentException(
                    "Room already exists with number: "
                            + request.roomNumber()
            );
        }

        roomMapper.updateEntity(room, request);

        return roomMapper.toResponse(
                roomRepository.save(room)
        );
    }

    @Override
    @Transactional
    public void deleteRoom(
            UUID hostelId,
            UUID roomId
    ) {

        Room room = findRoom(hostelId, roomId);

        if (!room.getBeds().isEmpty()) {

            throw new IllegalArgumentException(
                    "Cannot delete room because beds exist in this room"
            );
        }

        roomRepository.delete(room);
    }

    private Hostel findHostel(UUID hostelId) {

        return hostelRepository.findById(hostelId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Hostel not found with id: "
                                        + hostelId
                        ));
    }

    private Room findRoom(
            UUID hostelId,
            UUID roomId
    ) {

        return roomRepository
                .findByIdAndHostelId(roomId, hostelId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Room not found with id: "
                                        + roomId
                        ));
    }

    private void validateFloor(
            Hostel hostel,
            Integer floor
    ) {

        if (floor > hostel.getTotalFloors()) {

            throw new IllegalArgumentException(
                    "Floor " + floor
                            + " exceeds hostel's total floors: "
                            + hostel.getTotalFloors()
            );
        }
    }

    private void validateCapacity(
            com.hostelhub.hostelservice.entity.RoomType type,
            Integer capacity
    ) {

        int expectedCapacity = switch (type) {

            case SINGLE -> 1;

            case DOUBLE -> 2;

            case TRIPLE -> 3;

            case FOUR_SHARING -> 4;

            case FIVE_SHARING -> 5;
        };

        if (capacity != expectedCapacity) {

            throw new IllegalArgumentException(
                    "Capacity for " + type
                            + " room must be "
                            + expectedCapacity
            );
        }
    }
}