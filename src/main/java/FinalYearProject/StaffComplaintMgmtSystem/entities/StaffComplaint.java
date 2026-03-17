package FinalYearProject.StaffComplaintMgmtSystem.entities;

import FinalYearProject.StaffComplaintMgmtSystem.enums.Category;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Priority;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "staff_complaints")
public class StaffComplaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Category category = Category.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.OPEN;

    // ----------------------------------------------------------------
    // SUBMITTER INFO (populated from authenticated UserInfo at submit time)
    // ----------------------------------------------------------------
    @Column(name = "submitted_by_staff_id")
    private String submittedByStaffId;

    @Column(name = "submitted_by_name")
    private String submittedByName;

    @Column(name = "submitted_by_email")
    private String submittedByEmail;

    // ----------------------------------------------------------------
    // ADMIN / HANDLER INFO
    // ----------------------------------------------------------------
    @Column(columnDefinition = "TEXT")
    private String adminResponse;

    @Column(name = "handled_by_id")
    private String handledById;

    @Column(name = "handled_by_name")
    private String handledByName;

    // ----------------------------------------------------------------
    // ASSIGNMENT (optional: assigned to a manager)
    // ----------------------------------------------------------------
    @Column(name = "assigned_manager_id")
    private String assignedManagerId;

    @Column(name = "assigned_manager_name")
    private String assignedManagerName;

    // ----------------------------------------------------------------
    // TIMESTAMPS
    // ----------------------------------------------------------------
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
