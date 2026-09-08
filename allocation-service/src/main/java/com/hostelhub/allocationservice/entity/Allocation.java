package com.hostelhub.allocationservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "allocations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Allocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /*
     * Optimistic lock version.
     *
     * Prevents two concurrent createAllocation() calls from both passing the
     * bed-availability check and occupying the same bed simultaneously.
     */
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    /*
     * ID of the student from identity-service.
     * No foreign key because this belongs to another microservice.
     */
    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    /*
     * ID of the bed from hostel-service.
     * No JPA relationship because Bed belongs to another microservice.
     */
    @Column(name = "bed_id", nullable = false)
    private UUID bedId;

    @Column(name = "allocated_date", nullable = false)
    private LocalDate allocatedDate;

    @Column(name = "checkout_date")
    private LocalDate checkoutDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AllocationStatus status = AllocationStatus.ACTIVE;

    @Column(length = 500)
    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "hostel_id", nullable =false)
    private UUID hostelId;

    @PrePersist
    protected void onCreate() {

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (allocatedDate == null) {
            allocatedDate = LocalDate.now();
        }

        if (status == null) {
            status = AllocationStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();

    }
}