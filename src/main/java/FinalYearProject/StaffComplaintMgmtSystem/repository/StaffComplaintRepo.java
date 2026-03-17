package FinalYearProject.StaffComplaintMgmtSystem.repository;

import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffComplaint;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Category;
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

    // Admin: filter by status
    Page<StaffComplaint> findByStatus(Status status, Pageable pageable);

    // Admin: filter by priority
    Page<StaffComplaint> findByPriority(Priority priority, Pageable pageable);

    // Admin: filter by category
    Page<StaffComplaint> findByCategory(Category category, Pageable pageable);

    // Admin: complaints assigned to a manager
    List<StaffComplaint> findByAssignedManagerId(String managerId);

    // Dashboard stats
    long countByStatus(Status status);
    long countByPriority(Priority priority);
    long countBySubmittedByStaffId(String staffId);

    // Admin: search by title keyword
    @Query("SELECT c FROM StaffComplaint c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<StaffComplaint> searchByTitle(@Param("keyword") String keyword, Pageable pageable);
}
