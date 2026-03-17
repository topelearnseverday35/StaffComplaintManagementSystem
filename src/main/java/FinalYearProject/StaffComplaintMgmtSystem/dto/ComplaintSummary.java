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
// RESPONSE: Summary/list view (lighter payload)
// ─────────────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class ComplaintSummary {
    private Long id;
    private String title;
    private Category category;
    private Priority priority;
    private Status status;
    private String submittedByName;
    private LocalDateTime createdAt;
}
