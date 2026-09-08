package com.hostelhub.hostelservice.repository;

import com.hostelhub.hostelservice.entity.HostelWarden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HostelWardenRepository extends JpaRepository<HostelWarden, UUID> {

    /** All active warden assignments for a hostel. */
    List<HostelWarden> findByHostelIdAndActiveTrue(UUID hostelId);

    /** All active hostel assignments for a warden (should normally be at most one). */
    List<HostelWarden> findByWardenIdAndActiveTrue(UUID wardenId);

    /** Check if a specific warden-hostel link already exists (active or inactive). */
    boolean existsByHostelIdAndWardenId(UUID hostelId, UUID wardenId);

    /** Find a specific warden-hostel assignment regardless of active status. */
    Optional<HostelWarden> findByHostelIdAndWardenId(UUID hostelId, UUID wardenId);

    /** Find the current active assignment for a warden. */
    Optional<HostelWarden> findByWardenIdAndActiveTrue(UUID wardenId);
}
