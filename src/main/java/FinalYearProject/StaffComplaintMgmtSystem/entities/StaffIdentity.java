package FinalYearProject.StaffComplaintMgmtSystem.entities;

import FinalYearProject.StaffComplaintMgmtSystem.enums.Department;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Roles;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Data
@Table(name = "staff_identity_table")
public class StaffIdentity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private long id;

    @Column(unique = true, nullable = false)
    private String staffId;

    @Column(name = "staff_first_name")
    private String staffFirstName;

    @Column(name = "staff_last_name")
    private String staffLastName;

    @Column(name = "staff_email")
    private String staffEmail;

    @Column(name = "staff_phone_number")
    private String staffPhoneNumber;

    @Column(name = "staff_address")
    private String staffAddress;

    @Column(name = "staff_Password")
    private String staffPassword;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Roles role;

    /**
     * The Babcock University department this staff member belongs to.
     * HOD  -> sees only HOD_LEVEL complaints from staff in the SAME department.
     * DEAN -> sees DEAN_LEVEL/HOD complaints from the SAME school (department.getSchool()).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "department")
    private Department department;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) return List.of();
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override public String getPassword()  { return staffPassword; }
    @Override public String getUsername()  { return staffEmail; }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}
