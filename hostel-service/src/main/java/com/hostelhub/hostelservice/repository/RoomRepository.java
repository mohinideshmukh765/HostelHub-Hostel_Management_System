package com.hostelhub.hostelservice.repository;

import com.hostelhub.hostelservice.entity.HostelStatus;
import com.hostelhub.hostelservice.entity.Room;
import com.hostelhub.hostelservice.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    boolean existsByHostelIdAndRoomNumber(
            UUID hostelId,
            String roomNumber
    );

    Optional<Room> findByIdAndHostelId(
            UUID roomId,
            UUID hostelId
    );

    List<Room> findByHostelId(
            UUID hostelId
    );

    List<Room> findByHostelIdAndStatus(
            UUID hostelId,
            HostelStatus status
    );

    List<Room> findByHostelIdAndFloor(
            UUID hostelId,
            Integer floor
    );

    List<Room> findByHostelIdAndType(
            UUID hostelId,
            RoomType type
    );

    long countByHostelId(UUID hostelId);

    long countByHostelIdAndStatus(
            UUID hostelId,
            HostelStatus status
    );
}