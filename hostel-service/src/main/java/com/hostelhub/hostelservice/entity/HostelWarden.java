package com.hostelhub.hostelservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Maps a warden to a hostel.
 * A warden may be assigned to only one hostel at a time (unique constraint on warden_id).
 * Multiple wardens can be assigned to the same hostel (no such constraint).
 *
 * NOTE: The authoritative hostelId for a warden is stored on User.hostelId in
 * identity-service. This table is the hostel-service-side record of the same
 * assignment and can be used for auditing/reporting. The two must be kept in sync
 * when a warden assignment is changed.
 */
@Entity
@Table(
        name = "hostel_wardens",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_hostel_warden",
                        columnNames = {"hostel_id", "warden_id"}
                )
        },
        indexes = {
                @Index(name = "idx_hostel_warden_hostel", columnList = "hostel_id"),
                @Index(name = "idx_hostel_warden_warden", columnList = "warden_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostelWarden {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** References hostel_db.hostels.id (same service — this IS a real FK). */
    @Column(name = "hostel_id", nullable = false)
    private UUID hostelId;

    /**
     * References identity-service's users.id.
     * No DB-level FK across service boundaries.
     */
    @Column(name = "warden_id", nullable = false)
    private UUID wardenId;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}