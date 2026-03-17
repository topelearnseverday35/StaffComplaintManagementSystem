package FinalYearProject.StaffComplaintMgmtSystem.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeleteStaffRequest {
    private  String phoneNumber;
    @NotBlank(message = "staffId Can Not Be Blank")
    private  String staffId;
    private String  email;
}
