package FinalYearProject.StaffComplaintMgmtSystem.repository;

import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffIdentityRepo extends JpaRepository<StaffIdentity, Long> {

    Optional<StaffIdentity> findByStaffId(String staffId);
    Optional<StaffIdentity>findByStaffEmail(String staffEmail);
    Optional<StaffIdentity> findByStaffIdAndStaffPhoneNumber(String staffId, String phoneNumber);
  Optional<StaffIdentity> findByStaffIdAndStaffEmail(String staffId, String Email);
}
