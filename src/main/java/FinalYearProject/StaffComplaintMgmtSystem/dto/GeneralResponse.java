package FinalYearProject.StaffComplaintMgmtSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GeneralResponse {
    private String responseMsg;
    private String timeStamp  = String.valueOf(System.currentTimeMillis());
}