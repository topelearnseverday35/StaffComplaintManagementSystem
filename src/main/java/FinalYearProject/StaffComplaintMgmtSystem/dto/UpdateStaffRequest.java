package FinalYearProject.StaffComplaintMgmtSystem.dto;

import lombok.Data;

@Data
public class UpdateStaffRequest {
    private String staffId;
    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String address;
}
