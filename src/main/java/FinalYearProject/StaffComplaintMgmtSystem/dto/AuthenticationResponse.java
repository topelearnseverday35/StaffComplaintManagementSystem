package FinalYearProject.StaffComplaintMgmtSystem.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class AuthenticationResponse {
    private String token;
    private String response;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Object> user;
}
