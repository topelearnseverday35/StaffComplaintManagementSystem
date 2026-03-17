package FinalYearProject.StaffComplaintMgmtSystem.dto;

import FinalYearProject.StaffComplaintMgmtSystem.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// ─────────────────────────────────────────────
// REQUEST: Admin updates/responds to a complaint
// ─────────────────────────────────────────────
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class UpdateComplaintRequest {
    private Long id;               // ← ADD THIS — replaces @PathVariable
    private Status status;
    private String adminResponse;
    private String assignedManagerId;
    private String assignedManagerName;
}
