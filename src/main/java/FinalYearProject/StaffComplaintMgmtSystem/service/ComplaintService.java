package FinalYearProject.StaffComplaintMgmtSystem.service;


import FinalYearProject.StaffComplaintMgmtSystem.dto.ComplaintResponse;
import FinalYearProject.StaffComplaintMgmtSystem.dto.ComplaintSummary;
import FinalYearProject.StaffComplaintMgmtSystem.dto.SubmitComplaintRequest;
import FinalYearProject.StaffComplaintMgmtSystem.dto.UpdateComplaintRequest;
import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffComplaint;
import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffIdentity;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Status;

import FinalYearProject.StaffComplaintMgmtSystem.events.ComplaintAssignedEvents;
import FinalYearProject.StaffComplaintMgmtSystem.events.ComplaintStatusUpdatedEvents;
import FinalYearProject.StaffComplaintMgmtSystem.events.ComplaintSubmittedEvents;
import FinalYearProject.StaffComplaintMgmtSystem.repository.StaffComplaintRepo;
import FinalYearProject.StaffComplaintMgmtSystem.repository.StaffIdentityRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComplaintService {

    private final StaffComplaintRepo complaintRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final StaffIdentityRepo repo;

    // ─────────────────────────────────────────────────────────
    // STAFF: Submit a complaint
    // ─────────────────────────────────────────────────────────
    @Transactional
    public ComplaintResponse submitComplaint(SubmitComplaintRequest request) {
        log.info("Submit Complaint Request - {}",request);
        StaffIdentity currentUser = getAuthenticatedUser();
        log.info(" User - {} has been authenticated",currentUser.getStaffFirstName());

        StaffComplaint complaint = StaffComplaint.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory() != null ? request.getCategory() :
                        FinalYearProject.StaffComplaintMgmtSystem.enums.Category.GENERAL)
                .priority(request.getPriority() != null ? request.getPriority() :
                        FinalYearProject.StaffComplaintMgmtSystem.enums.Priority.MEDIUM)
                .status(Status.OPEN)
                .submittedByStaffId(currentUser.getStaffId())
                .submittedByName(currentUser.getStaffFirstName() + " " + currentUser.getStaffLastName())
                .submittedByEmail(currentUser.getStaffEmail())
                .build();

        StaffComplaint saved = complaintRepository.save(complaint);
        log.info("Complaint #{} submitted by staff {}", saved.getId(), currentUser.getStaffId());

        // Fire event → triggers email to staff + all admins
        eventPublisher.publishEvent(new ComplaintSubmittedEvents(
                this, saved, currentUser.getStaffEmail(),
                currentUser.getStaffFirstName() + " " + currentUser.getStaffLastName()
        ));

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────────────────
    // STAFF: View own complaints (paginated)
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ComplaintSummary> getMyComplaints(Pageable pageable) {
        StaffIdentity currentUser = getAuthenticatedUser();
        return complaintRepository
                .findBySubmittedByStaffId(currentUser.getStaffId(), pageable)
                .map(this::toSummary);
    }

    // ─────────────────────────────────────────────────────────
    // STAFF: View one of their own complaints
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ComplaintResponse getMyComplaintById(Long id) {
        StaffIdentity currentUser = getAuthenticatedUser();
        StaffComplaint complaint = findById(id);
        if (!complaint.getSubmittedByStaffId().equals(currentUser.getStaffId())) {
            throw new SecurityException("You do not have access to this complaint");
        }
        return toResponse(complaint);
    }

    // ─────────────────────────────────────────────────────────
    // ADMIN: Get all complaints (paginated, optional status filter)
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ComplaintSummary> getAllComplaints(Status statusFilter, Pageable pageable) {
        if (statusFilter != null) {
            return complaintRepository.findByStatus(statusFilter, pageable).map(this::toSummary);
        }
        return complaintRepository.findAll(pageable).map(this::toSummary);
    }

    // ─────────────────────────────────────────────────────────
    // ADMIN: Get single complaint by ID
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id) {
        return toResponse(findById(id));
    }

    // ─────────────────────────────────────────────────────────
    // ADMIN: Update complaint status / add response / assign manager
    // ─────────────────────────────────────────────────────────
    @Transactional
    public ComplaintResponse updateComplaint(Long id, UpdateComplaintRequest request) {
        StaffIdentity admin = getAuthenticatedUser();
        StaffComplaint complaint = findById(id);
        Status previousStatus = complaint.getStatus();

        if (request.getStatus() != null) {
            complaint.setStatus(request.getStatus());
            if (request.getStatus() == Status.RESOLVED) {
                complaint.setResolvedAt(LocalDateTime.now());
            }
        }
        if (request.getAdminResponse() != null) {
            complaint.setAdminResponse(request.getAdminResponse());
        }

        complaint.setHandledById(admin.getStaffId());
        complaint.setHandledByName(admin.getStaffFirstName() + " " + admin.getStaffLastName());

        // Handle manager assignment
        boolean isNewAssignment = false;
        if (request.getAssignedManagerId() != null && !request.getAssignedManagerId().isBlank()) {
            isNewAssignment = !request.getAssignedManagerId().equals(complaint.getAssignedManagerId());
            complaint.setAssignedManagerId(request.getAssignedManagerId());
            complaint.setAssignedManagerName(request.getAssignedManagerName());
        }

        StaffComplaint updated = complaintRepository.save(complaint);
        log.info("Complaint #{} updated by admin {}", id, admin.getStaffId());

        // Fire status update event → notifies original submitter
        if (request.getStatus() != null && !previousStatus.equals(request.getStatus())) {
            eventPublisher.publishEvent(new ComplaintStatusUpdatedEvents(
                    this, updated, previousStatus,
                    complaint.getSubmittedByEmail(),
                    complaint.getSubmittedByName()
            ));
        }

        // Fire assignment event → notifies manager
        if (isNewAssignment) {
            repo.findByStaffId(request.getAssignedManagerId())
                    .ifPresent(manager -> eventPublisher.publishEvent(new ComplaintAssignedEvents(
                            this, updated,
                            manager.getStaffEmail(),
                            manager.getStaffFirstName() + " " + manager.getStaffLastName(),
                            admin.getStaffFirstName() + " " + admin.getStaffLastName()
                    )));
        }

        return toResponse(updated);
    }

    // ─────────────────────────────────────────────────────────
    // ADMIN: Delete a complaint
    // ─────────────────────────────────────────────────────────
    @Transactional
    public void deleteComplaint(Long id) {
        StaffComplaint complaint = findById(id);
        complaintRepository.delete(complaint);
        log.info("Complaint #{} deleted", id);
    }

    // ─────────────────────────────────────────────────────────
    // ADMIN: Search by title keyword
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ComplaintSummary> searchComplaints(String keyword, Pageable pageable) {
        return complaintRepository.searchByTitle(keyword, pageable).map(this::toSummary);
    }

    // ─────────────────────────────────────────────────────────
    // DASHBOARD STATS (admin)
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Map<String, Long> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", complaintRepository.count());
        stats.put("open", complaintRepository.countByStatus(Status.OPEN));
        stats.put("inProgress", complaintRepository.countByStatus(Status.IN_PROGRESS));
        stats.put("resolved", complaintRepository.countByStatus(Status.RESOLVED));
        stats.put("closed", complaintRepository.countByStatus(Status.CLOSED));
        return stats;
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────
    private StaffComplaint findById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found with id: " + id));
    }

    private StaffIdentity getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof StaffIdentity) {
            return (StaffIdentity) principal;
        }
        throw new EntityNotFoundException("Authenticated user not found in security context");
    }

    private ComplaintResponse toResponse(StaffComplaint c) {
        return ComplaintResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .category(c.getCategory())
                .priority(c.getPriority())
                .status(c.getStatus())
                .adminResponse(c.getAdminResponse())
                .handledById(c.getHandledById())
                .handledByName(c.getHandledByName())
                .submittedByStaffId(c.getSubmittedByStaffId())
                .submittedByName(c.getSubmittedByName())
                .submittedByEmail(c.getSubmittedByEmail())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .resolvedAt(c.getResolvedAt())
                .build();
    }

    private ComplaintSummary toSummary(StaffComplaint c) {
        return ComplaintSummary.builder()
                .id(c.getId())
                .title(c.getTitle())
                .category(c.getCategory())
                .priority(c.getPriority())
                .status(c.getStatus())
                .submittedByName(c.getSubmittedByName())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
