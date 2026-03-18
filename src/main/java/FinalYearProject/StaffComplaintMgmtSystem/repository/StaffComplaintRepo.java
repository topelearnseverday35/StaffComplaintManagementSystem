package FinalYearProject.StaffComplaintMgmtSystem.repository;

import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffComplaint;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Category;
import FinalYearProject.StaffComplaintMgmtSystem.enums.EscalationLevel;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Priority;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffComplaintRepo extends JpaRepository<StaffComplaint, Long> {

    // Staff: view their own complaints
    Page<StaffComplaint> findBySubmittedByStaffId(String staffId, Pageable pageable);

    // HOD: see only complaints at HOD_LEVEL (i.e. submitted by LECTURERs, not yet escalated)
    Page<StaffComplaint> findByEscalationLevel(EscalationLevel escalationLevel, Pageable pageable);

    // HOD: filter by status AND escalation level
    Page<StaffComplaint> findByEscalationLevelAndStatus(EscalationLevel escalationLevel, Status status, Pageable pageable);

    // DEAN: see only complaints at DEAN_LEVEL (escalated by HOD)
    // Note: DEAN also sees HOD's own complaints (submittedByRole = 'HOD')
    @Query("""
        SELECT c FROM StaffComplaint c
        WHERE c.escalationLevel = 'DEAN_LEVEL'
           OR c.submittedByRole = 'HOD'
        """)
    Page<StaffComplaint> findComplaintsVisibleToDean(Pageable pageable);

    @Query("""
        SELECT c FROM StaffComplaint c
        WHERE (c.escalationLevel = 'DEAN_LEVEL' OR c.submittedByRole = 'HOD')
          AND c.status = :status
        """)
    Page<StaffComplaint> findComplaintsVisibleToDeanByStatus(@Param("status") Status status, Pageable pageable);

    // Admin: filter by status only (used by role-aware logic in service)
    Page<StaffComplaint> findByStatus(Status status, Pageable pageable);

    // Admin: filter by priority
    Page<StaffComplaint> findByPriority(Priority priority, Pageable pageable);

    // Admin: filter by category
    Page<StaffComplaint> findByCategory(Category category, Pageable pageable);

    // Complaints assigned to a specific manager
    List<StaffComplaint> findByAssignedManagerId(String managerId);

    // Dashboard stats
    long countByStatus(Status status);
    long countByPriority(Priority priority);
    long countBySubmittedByStaffId(String staffId);
    long countByEscalationLevel(EscalationLevel escalationLevel);

    // Search by title keyword — role-aware (HOD sees HOD_LEVEL, Dean sees DEAN_LEVEL + HOD submissions)
    @Query("""
        SELECT c FROM StaffComplaint c
        WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
          AND c.escalationLevel = :escalationLevel
        """)
    Page<StaffComplaint> searchByTitleForHod(
            @Param("keyword") String keyword,
            @Param("escalationLevel") EscalationLevel escalationLevel,
            Pageable pageable);

    @Query("""
        SELECT c FROM StaffComplaint c
        WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
          AND (c.escalationLevel = 'DEAN_LEVEL' OR c.submittedByRole = 'HOD')
        """)
    Page<StaffComplaint> searchByTitleForDean(@Param("keyword") String keyword, Pageable pageable);
}
