package FinalYearProject.StaffComplaintMgmtSystem.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDTO {
    private Integer page;
    private Integer size;
}
