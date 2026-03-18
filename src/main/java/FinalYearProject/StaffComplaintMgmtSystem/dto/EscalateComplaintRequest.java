package FinalYearProject.StaffComplaintMgmtSystem.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for HOD to escalate a complaint to the Dean.
 * Body: { "id": 1, "escalationNote": "This requires Dean-level attention because..." }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EscalateComplaintRequest {

    @NotNull(message = "Complaint id is required")
    private Long id;

    /** Optional note explaining why this is being escalated */
    private String escalationNote;
}
