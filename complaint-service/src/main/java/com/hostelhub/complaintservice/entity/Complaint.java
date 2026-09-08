package com.hostelhub.complaintservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "complaints",
        indexes = {
                @Index(name = "idx_complaint_student", columnList = "student_id"),
                @Index(name = "idx_complaint_status", columnList = "status"),
                @Index(name = "idx_complaint_category", columnList = "category")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /*
     * ID of the student from identity-service.
     * No cross-service foreign key.
     */
    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ComplaintCategory category;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ComplaintPriority priority = ComplaintPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.OPEN;

    /*
     * ID of the warden handling the complaint.
     * No cross-service foreign key.
     */

    @Column(name = "hostel_id", nullable = false)
    private UUID hostelId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "bed_id", nullable = false)
    private UUID bedId;

    @Column(name = "handled_by")
    private UUID handledBy;

    @Column(columnDefinition = "TEXT")
    private String resolutionRemarks;

    @Column(name="closed_by")
    private UUID closedBy;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name="escalated",nullable=false)
    @Builder.Default
    private Boolean escalated=false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime resolvedAt;

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