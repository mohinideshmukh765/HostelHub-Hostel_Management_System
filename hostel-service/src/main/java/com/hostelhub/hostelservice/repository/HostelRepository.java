package com.hostelhub.hostelservice.repository;

import com.hostelhub.hostelservice.entity.Hostel;
import com.hostelhub.hostelservice.entity.HostelStatus;
import com.hostelhub.hostelservice.entity.HostelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HostelRepository extends JpaRepository<Hostel, UUID> {

    boolean existsByNameIgnoreCase(String name);

    Page<Hostel> findByStatus(
            HostelStatus status,
            Pageable pageable
    );

    Page<Hostel> findByType(
            HostelType type,
            Pageable pageable
    );

    Page<Hostel> findByCityIgnoreCase(
            String city,
            Pageable pageable
    );

    Page<Hostel> findByStatusAndCityIgnoreCase(
            HostelStatus status,
            String city,
            Pageable pageable
    );

    Page<Hostel> findByTypeAndCityIgnoreCase(
            HostelType type,
            String city,
            Pageable pageable
    );

    Page<Hostel> findByStatusAndTypeAndCityIgnoreCase(
            HostelStatus status,
            HostelType type,
            String city,
            Pageable pageable
    );

    Page<Hostel> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    Page<Hostel> findByNameContainingIgnoreCaseAndCityIgnoreCase(
            String name,
            String city,
            Pageable pageable
    );
}