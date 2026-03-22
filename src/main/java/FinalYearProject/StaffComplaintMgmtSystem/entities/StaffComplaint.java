package FinalYearProject.StaffComplaintMgmtSystem.entities;

import FinalYearProject.StaffComplaintMgmtSystem.enums.Category;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Department;
import FinalYearProject.StaffComplaintMgmtSystem.enums.EscalationLevel;
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
    @Size(min = 5, max = 200)
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000)
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

    // ── ESCALATION ────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "escalation_level", nullable = false)
    @Builder.Default
    private EscalationLevel escalationLevel = EscalationLevel.HOD_LEVEL;

    @Column(name = "escalated_by_staff_id")
    private String escalatedByStaffId;

    @Column(name = "escalated_by_name")
    private String escalatedByName;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "escalation_note", columnDefinition = "TEXT")
    private String escalationNote;

    // ── DEPARTMENT SCOPING ────────────────────────────────────────────────
    /**
     * Department of the staff member who submitted this complaint.
     * HODs filter by: department == submitter's department
     * Deans filter by: department.getSchool() == dean's school
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "submitted_by_department")
    private Department submittedByDepartment;

    /** School name derived from department — stored for efficient Dean-level queries */
    @Column(name = "submitted_by_school")
    private String submittedBySchool;

    // ── SUBMITTER INFO ────────────────────────────────────────────────────
    @Column(name = "submitted_by_staff_id")
    private String submittedByStaffId;

    @Column(name = "submitted_by_name")
    private String submittedByName;

    @Column(name = "submitted_by_email")
    private String submittedByEmail;

    @Column(name = "submitted_by_role")
    private String submittedByRole;

    // ── ADMIN / HANDLER ───────────────────────────────────────────────────
    @Column(columnDefinition = "TEXT")
    private String adminResponse;

    @Column(name = "handled_by_id")
    private String handledById;

    @Column(name = "handled_by_name")
    private String handledByName;

    @Column(name = "assigned_manager_id")
    private String assignedManagerId;

    @Column(name = "assigned_manager_name")
    private String assignedManagerName;

    // ── TIMESTAMPS ────────────────────────────────────────────────────────
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
