package FinalYearProject.StaffComplaintMgmtSystem.dto;

import FinalYearProject.StaffComplaintMgmtSystem.enums.Category;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Priority;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// ─────────────────────────────────────────────
// RESPONSE: Complaint data returned to client
// ─────────────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class ComplaintResponse {
    private Long id;
    private String title;
    private String description;
    private Category category;
    private Priority priority;
    private Status status;
    private String adminResponse;
    private String handledById;
    private String handledByName;
    private String submittedByStaffId;
    private String submittedByName;
    private String submittedByEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}

