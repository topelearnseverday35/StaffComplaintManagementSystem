package FinalYearProject.StaffComplaintMgmtSystem.repository;

import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffComplaint;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Category;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Department;
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

    // ── Own complaints (any role) ─────────────────────────────────────────
    Page<StaffComplaint> findBySubmittedByStaffId(String staffId, Pageable pageable);

    // ── HOD: HOD_LEVEL within same department ─────────────────────────────
    Page<StaffComplaint> findByEscalationLevelAndSubmittedByDepartment(
            EscalationLevel level, Department dept, Pageable pageable);

    Page<StaffComplaint> findByEscalationLevelAndStatusAndSubmittedByDepartment(
            EscalationLevel level, Status status, Department dept, Pageable pageable);

    // ── DEAN: DEAN_LEVEL or HOD-submitted complaints within same school ───
    @Query("""
        SELECT c FROM StaffComplaint c
        WHERE (c.escalationLevel = 'DEAN_LEVEL' OR c.submittedByRole = 'HOD')
          AND c.submittedBySchool = :school
        """)
    Page<StaffComplaint> findComplaintsVisibleToDean(
            @Param("school") String school, Pageable pageable);

    @Query("""
        SELECT c FROM StaffComplaint c
        WHERE (c.escalationLevel = 'DEAN_LEVEL' OR c.submittedByRole = 'HOD')
          AND c.submittedBySchool = :school
          AND c.status = :status
        """)
    Page<StaffComplaint> findComplaintsVisibleToDeanByStatus(
            @Param("school") String school,
            @Param("status") Status status,
            Pageable pageable);

    // ── Older queries kept for stats / assignment ─────────────────────────
    Page<StaffComplaint> findByEscalationLevel(EscalationLevel escalationLevel, Pageable pageable);
    Page<StaffComplaint> findByEscalationLevelAndStatus(EscalationLevel escalationLevel, Status status, Pageable pageable);
    Page<StaffComplaint> findByStatus(Status status, Pageable pageable);
    Page<StaffComplaint> findByPriority(Priority priority, Pageable pageable);
    Page<StaffComplaint> findByCategory(Category category, Pageable pageable);
    List<StaffComplaint> findByAssignedManagerId(String managerId);

    // ── Stats ─────────────────────────────────────────────────────────────
    long countByStatus(Status status);
    long countByPriority(Priority priority);
    long countBySubmittedByStaffId(String staffId);
    long countByEscalationLevel(EscalationLevel escalationLevel);

    @Query("SELECT COUNT(c) FROM StaffComplaint c WHERE c.escalationLevel = :level AND c.submittedByDepartment = :dept")
    long countByEscalationLevelAndDepartment(@Param("level") EscalationLevel level, @Param("dept") Department dept);

    @Query("SELECT COUNT(c) FROM StaffComplaint c WHERE c.status = :status AND c.submittedByDepartment = :dept")
    long countByStatusAndDepartment(@Param("status") Status status, @Param("dept") Department dept);

    @Query("SELECT COUNT(c) FROM StaffComplaint c WHERE (c.escalationLevel = 'DEAN_LEVEL' OR c.submittedByRole = 'HOD') AND c.submittedBySchool = :school")
    long countVisibleToDean(@Param("school") String school);

    @Query("SELECT COUNT(c) FROM StaffComplaint c WHERE (c.escalationLevel = 'DEAN_LEVEL' OR c.submittedByRole = 'HOD') AND c.submittedBySchool = :school AND c.status = :status")
    long countVisibleToDeanByStatus(@Param("school") String school, @Param("status") Status status);

    // ── Search ────────────────────────────────────────────────────────────
    @Query("""
        SELECT c FROM StaffComplaint c
        WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
          AND c.escalationLevel = 'HOD_LEVEL'
          AND c.submittedByDepartment = :dept
        """)
    Page<StaffComplaint> searchByTitleForHod(
            @Param("keyword") String keyword,
            @Param("dept") Department dept,
            Pageable pageable);

    @Query("""
        SELECT c FROM StaffComplaint c
        WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
          AND (c.escalationLevel = 'DEAN_LEVEL' OR c.submittedByRole = 'HOD')
          AND c.submittedBySchool = :school
        """)
    Page<StaffComplaint> searchByTitleForDean(
            @Param("keyword") String keyword,
            @Param("school") String school,
            Pageable pageable);
}
