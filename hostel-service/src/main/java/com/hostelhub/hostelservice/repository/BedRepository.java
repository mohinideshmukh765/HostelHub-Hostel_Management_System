package com.hostelhub.hostelservice.repository;

import com.hostelhub.hostelservice.entity.Bed;
import com.hostelhub.hostelservice.entity.BedStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BedRepository extends JpaRepository<Bed, UUID> {

    Optional<Bed> findById(UUID id);



    boolean existsByRoomIdAndBedNumber(
            UUID roomId,
            String bedNumber
    );



    Optional<Bed> findByIdAndRoomId(
            UUID bedId,
            UUID roomId
    );

    List<Bed> findByRoomId(UUID roomId);

    List<Bed> findByRoomIdAndStatus(
            UUID roomId,
            BedStatus status
    );

    List<Bed> findByStatus(BedStatus status);

    long countByRoomId(UUID roomId);

    long countByRoomIdAndStatus(
            UUID roomId,
            BedStatus status
    );
}