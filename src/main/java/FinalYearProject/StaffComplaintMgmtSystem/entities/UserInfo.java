//package FinalYearProject.StaffComplaintMgmtSystem.entities;
//
//
//import FinalYearProject.StaffComplaintMgmtSystem.enums.Roles;
//import FinalYearProject.StaffComplaintMgmtSystem.enums.isActive;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//
//import jakarta.persistence.*;
//import lombok.Data;
//import org.hibernate.envers.Audited;
//
//import java.time.LocalDateTime;
//import java.util.Collection;
//import java.util.List;
//
//@Entity
//@Audited
//@Data
//@Table(name = "userinfo")
//public class UserInfo implements UserDetails {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;
//
//    @Column(name = "staff_id")
//    private String staffId;
//
//    @Column(name = "firstName", nullable = false)
//    private String firstName;
//
//    @Column(name = "middleName", nullable = false)
//    private String middleName;
//
//    @Column(name = "lastName")
//    private String lastName;
//
//    @Column(name = "email", nullable = false, unique = true)
//    private String email;
//
//    @Column(name = "password", nullable = false)
//    private String password; // should be hashed before saving
//
//    @Column(name = "phoneNumber")
//    private String phoneNumber;
//    @Enumerated(EnumType.STRING)
//    @Column(name = "role")
//    private Roles role;
////
////    @Enumerated(EnumType.STRING)
////    @Column(name = "jobTitle")
////    private JobTitle designation; // e.g., "Lecturer,HOD,DEAN"
//
//    @Column(name = "role_Id")
//    private String roleId;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "activationStatus")
//    private isActive activationStatus;
//
//    @Column(name = "createdAt", updatable = false)
//    private LocalDateTime createdAt;
//
//    @Column(name = "updatedAt")
//    private LocalDateTime updatedAt;
//
//
//    @PrePersist
//    protected void onCreate() {
//        this.createdAt = LocalDateTime.now();
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        this.updatedAt = LocalDateTime.now();
//    }
//
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return List.of(new SimpleGrantedAuthority(role.name()));
//    }
//
//    @Override
//    public String getPassword() {
//        return password;
//    }
//
//    @Override
//    public String getUsername() {
//        return email;
//    }
//
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }
//}
