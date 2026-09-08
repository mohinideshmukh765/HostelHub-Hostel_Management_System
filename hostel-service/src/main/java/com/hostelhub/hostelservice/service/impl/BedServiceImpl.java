package com.hostelhub.hostelservice.service.impl;

import com.hostelhub.hostelservice.dto.request.CreateBedRequest;
import com.hostelhub.hostelservice.dto.request.UpdateBedRequest;
import com.hostelhub.hostelservice.dto.response.BedResponse;
import com.hostelhub.hostelservice.dto.response.InternalBedResponse;
import com.hostelhub.hostelservice.entity.Bed;
import com.hostelhub.hostelservice.entity.BedStatus;
import com.hostelhub.hostelservice.entity.Hostel;
import com.hostelhub.hostelservice.entity.Room;
import com.hostelhub.hostelservice.exception.ResourceNotFoundException;
import com.hostelhub.hostelservice.mapper.BedMapper;
import com.hostelhub.hostelservice.repository.BedRepository;
import com.hostelhub.hostelservice.repository.RoomRepository;
import com.hostelhub.hostelservice.service.BedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BedServiceImpl implements BedService {

    private final BedRepository bedRepository;
    private final RoomRepository roomRepository;
    private final BedMapper bedMapper;

    @Override
    @Transactional
    public BedResponse createBed(
            UUID hostelId,
            UUID roomId,
            CreateBedRequest request
    ) {

        Room room = findRoom(hostelId, roomId);

        long currentBedCount =
                bedRepository.countByRoomId(roomId);

        if (currentBedCount >= room.getCapacity()) {

            throw new IllegalArgumentException(
                    "Cannot add more beds. Room capacity is "
                            + room.getCapacity()
            );
        }

        if (bedRepository.existsByRoomIdAndBedNumber(
                roomId,
                request.bedNumber().trim()
        )) {

            throw new IllegalArgumentException(
                    "Bed already exists with number: "
                            + request.bedNumber()
            );
        }

        Bed bed = bedMapper.toEntity(request);

        bed.setRoom(room);

        Bed savedBed = bedRepository.save(bed);

        return bedMapper.toResponse(savedBed);
    }

    @Override
    public BedResponse getBed(
            UUID hostelId,
            UUID roomId,
            UUID bedId
    ) {

        // First verifies that the room belongs to the hostel.
        findRoom(hostelId, roomId);

        Bed bed = findBed(roomId, bedId);

        return bedMapper.toResponse(bed);
    }

    @Override
    public List<BedResponse> searchBeds(
            UUID hostelId,
            UUID roomId,
            BedStatus status
    ) {

        findRoom(hostelId, roomId);

        List<Bed> beds;

        if (status != null) {

            beds = bedRepository
                    .findByRoomIdAndStatus(
                            roomId,
                            status
                    );

        } else {

            beds = bedRepository
                    .findByRoomId(roomId);
        }

        return beds.stream()
                .map(bedMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public BedResponse updateBed(
            UUID hostelId,
            UUID roomId,
            UUID bedId,
            UpdateBedRequest request
    ) {

        findRoom(hostelId, roomId);

        Bed bed = findBed(roomId, bedId);

        boolean bedNumberChanged =
                !bed.getBedNumber()
                        .equalsIgnoreCase(
                                request.bedNumber().trim()
                        );

        if (bedNumberChanged &&
                bedRepository.existsByRoomIdAndBedNumber(
                        roomId,
                        request.bedNumber().trim()
                )) {

            throw new IllegalArgumentException(
                    "Bed already exists with number: "
                            + request.bedNumber()
            );
        }

        bedMapper.updateEntity(bed, request);

        return bedMapper.toResponse(
                bedRepository.save(bed)
        );
    }

    @Override
    @Transactional
    public void deleteBed(
            UUID hostelId,
            UUID roomId,
            UUID bedId
    ) {

        findRoom(hostelId, roomId);

        Bed bed = findBed(roomId, bedId);

        if (bed.getStatus() == BedStatus.OCCUPIED) {

            throw new IllegalArgumentException(
                    "Cannot delete an occupied bed"
            );
        }

        bedRepository.delete(bed);
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
                                        + " in hostel: "
                                        + hostelId
                        ));
    }

    private Bed findBed(
            UUID roomId,
            UUID bedId
    ) {

        return bedRepository
                .findByIdAndRoomId(bedId, roomId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bed not found with id: "
                                        + bedId
                        ));
    }

    @Override
    @Transactional(readOnly = true)
    public BedResponse getBed(UUID id) {

        Bed bed = bedRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bed not found"
                        )
                );

        return bedMapper.toResponse(bed);
    }

    @Override
    @Transactional
    public BedResponse occupyBed(UUID id) {

        Bed bed = bedRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bed not found"
                        )
                );

        if (bed.getStatus() != BedStatus.AVAILABLE) {

            throw new IllegalArgumentException(
                    "Bed is not available"
            );
        }

        bed.setStatus(BedStatus.OCCUPIED);

        return bedMapper.toResponse(
                bedRepository.save(bed)
        );
    }

    @Override
    @Transactional
    public BedResponse releaseBed(UUID id) {

        Bed bed = bedRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bed not found"
                        )
                );

        bed.setStatus(BedStatus.AVAILABLE);

        return bedMapper.toResponse(
                bedRepository.save(bed)
        );
    }

    @Override
    public InternalBedResponse getInternalBed(UUID bedId) {
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Bed not found"
                        )
                );

        Room room = bed.getRoom();

        Hostel hostel = room.getHostel();


        return new InternalBedResponse(

                bed.getId(),

                room.getId(),

                hostel.getId(),

                hostel.getAdminId(),

                bed.getStatus()

        );
    }
}