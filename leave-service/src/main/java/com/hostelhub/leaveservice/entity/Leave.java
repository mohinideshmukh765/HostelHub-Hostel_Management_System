package com.hostelhub.leaveservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "leaves",
        indexes = {
                @Index(name = "idx_leave_student", columnList = "student_id"),
                @Index(name = "idx_leave_hostel", columnList = "hostel_id"),
                @Index(name = "idx_leave_status", columnList = "status"),
                @Index(name = "idx_leave_handled_by", columnList = "handled_by")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /*
     * Optimistic lock version.
     *
     * Prevents two wardens from concurrently "winning" the
     * acceptLeave() race, and prevents lost updates when the
     * same leave is modified concurrently elsewhere.
     */
    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "hostel_id", nullable = false)
    private UUID hostelId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "bed_id", nullable = false)
    private UUID bedId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    @Column(name = "handled_by")
    private UUID handledBy;

    @Column(name = "closed_by")
    private UUID closedBy;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(nullable = false)
    @Builder.Default
    private Boolean escalated = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "decision_at")
    private LocalDateTime decisionAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) status = LeaveStatus.PENDING;
        if (escalated == null) escalated = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}