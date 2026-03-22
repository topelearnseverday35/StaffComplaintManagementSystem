package FinalYearProject.StaffComplaintMgmtSystem.repository;

import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffIdentity;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Department;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffIdentityRepo extends JpaRepository<StaffIdentity, Long> {

    Optional<StaffIdentity> findByStaffId(String staffId);
    Optional<StaffIdentity> findByStaffEmail(String staffEmail);
    Optional<StaffIdentity> findByStaffIdAndStaffPhoneNumber(String staffId, String phoneNumber);
    Optional<StaffIdentity> findByStaffIdAndStaffEmail(String staffId, String email);

    /** All staff in a specific department */
    List<StaffIdentity> findByDepartment(Department department);

    /** All staff with a given role in a specific department */
    List<StaffIdentity> findByRoleAndDepartment(Roles role, Department department);

    /** All Deans whose department belongs to a given school name */
    @Query("SELECT s FROM StaffIdentity s WHERE s.role = 'DEAN' AND s.department IS NOT NULL")
    List<StaffIdentity> findAllDeans();
}
