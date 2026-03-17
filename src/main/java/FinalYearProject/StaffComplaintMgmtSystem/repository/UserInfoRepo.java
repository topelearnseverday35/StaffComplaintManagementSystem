//package FinalYearProject.StaffComplaintMgmtSystem.repository;
//
//import FinalYearProject.StaffComplaintMgmtSystem.entities.UserInfo;
//import FinalYearProject.StaffComplaintMgmtSystem.enums.Roles;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//@Repository
//public interface UserInfoRepo extends JpaRepository<UserInfo, Integer> {
//    Optional<UserInfo> findByEmail(String email);
//    Optional<UserInfo> findByStaffIdAndPhoneNumber(String staffId, String phoneNumber);
//    Optional<UserInfo> findByStaffIdAndEmail(String staffId, String Email);
//    List<UserInfo> findByRole(Roles role);
//    boolean existsByEmail(String email);
//    Optional<UserInfo> findByStaffId(String staffId);
//
//}
