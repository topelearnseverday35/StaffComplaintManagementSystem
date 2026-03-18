package FinalYearProject.StaffComplaintMgmtSystem.dto;

import FinalYearProject.StaffComplaintMgmtSystem.enums.Status;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminFilterRequest {
    private Status status;   // optional filter
    private Integer page;
    private Integer size;
}
