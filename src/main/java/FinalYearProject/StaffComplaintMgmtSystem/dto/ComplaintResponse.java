package FinalYearProject.StaffComplaintMgmtSystem.dto;

import FinalYearProject.StaffComplaintMgmtSystem.enums.Category;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Department;
import FinalYearProject.StaffComplaintMgmtSystem.enums.EscalationLevel;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Priority;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponse {
    private Long id;
    private String title;
    private String description;
    private Category category;
    private Priority priority;
    private Status status;
    private EscalationLevel escalationLevel;
    private String escalatedByName;
    private LocalDateTime escalatedAt;
    private String escalationNote;
    private String adminResponse;
    private String handledById;
    private String handledByName;
    private String submittedByStaffId;
    private String submittedByName;
    private String submittedByEmail;
    private String submittedByRole;
    private Department submittedByDepartment;
    private String submittedBySchool;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
}
