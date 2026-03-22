package FinalYearProject.StaffComplaintMgmtSystem.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Registrationrequest {

    @NotBlank(message = "FirstName Can Not Be Null")
    @Size(min = 2, max = 30, message = "First name must be between 2 and 30 characters")
    private String firstName;

    @NotBlank(message = "LastName Can Not Be Null")
    @Size(min = 2, max = 30, message = "Last name must be between 2 and 30 characters")
    private String lastName;

    @NotBlank(message = "Email Can Not Be Blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "PhoneNumber Can Not be Blank")
    @Pattern(regexp = "^0\\d{10}$", message = "Phone number must be 11 digits and start with 0")
    private String phoneNumber;

    @NotBlank(message = "Address can not be null")
    private String address;

    @Size(min = 8, max = 100, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=(?:.*\\d){2,})(?=.*[!@#$%^&*()_+{}\\[\\]:;<>,.?~\\-]).{8,}$",
            message = "Password must be at least 8 characters, 1 special character and 2 numbers"
    )
    private String password;

    @NotBlank(message = "Role Must Not Be Blank")
    private String role;

    /** Babcock University department — required for complaint scoping */
    @NotBlank(message = "Department must not be blank")
    private String department;
}
