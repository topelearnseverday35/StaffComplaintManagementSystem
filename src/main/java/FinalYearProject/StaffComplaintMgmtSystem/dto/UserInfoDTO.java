package FinalYearProject.StaffComplaintMgmtSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
    private Long id;
    private String username;
    private String email;
    private List<String> role;
    private String department;   // enum name e.g. "COMPUTING_COMPUTER_SCIENCE"
    private String school;       // display school e.g. "School of Computing & Engineering Sciences"
}
