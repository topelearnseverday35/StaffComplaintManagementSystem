package FinalYearProject.StaffComplaintMgmtSystem.service;

import FinalYearProject.StaffComplaintMgmtSystem.dto.*;
import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffComplaint;
import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffIdentity;
import FinalYearProject.StaffComplaintMgmtSystem.enums.EscalationLevel;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Roles;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Status;
import FinalYearProject.StaffComplaintMgmtSystem.events.*;
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
    private final StaffIdentityRepo staffIdentityRepo;

    // ─────────────────────────────────────────────────────────
    // STAFF / HOD: Submit a complaint
    // ─────────────────────────────────────────────────────────

    /**
     * Any authenticated staff (LECTURER or HOD) can submit a complaint.
     *
     * Visibility rules applied at submission time:
     *  - LECTURER submits → escalationLevel = HOD_LEVEL  → only HODs can see it
     *  - HOD submits      → submittedByRole = 'HOD'       → only DEANs can see it
     */
    @Transactional
    public ComplaintResponse submitComplaint(SubmitComplaintRequest request) {
        log.info("Submit Complaint Request - {}", request);
        StaffIdentity currentUser = getAuthenticatedUser();
        log.info("User - {} has been authenticated", currentUser.getStaffFirstName());

        // HOD complaints go straight to DEAN_LEVEL visibility
        EscalationLevel initialLevel = (currentUser.getRole() == Roles.HOD)
                ? EscalationLevel.DEAN_LEVEL
                : EscalationLevel.HOD_LEVEL;

        StaffComplaint complaint = StaffComplaint.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory() != null ? request.getCategory()
                        : FinalYearProject.StaffComplaintMgmtSystem.enums.Category.GENERAL)
                .priority(request.getPriority() != null ? request.getPriority()
                        : FinalYearProject.StaffComplaintMgmtSystem.enums.Priority.MEDIUM)
                .status(Status.OPEN)
                .escalationLevel(initialLevel)
                .submittedByStaffId(currentUser.getStaffId())
                .submittedByName(currentUser.getStaffFirstName() + " " + currentUser.getStaffLastName())
                .submittedByEmail(currentUser.getStaffEmail())
                .submittedByRole(currentUser.getRole().name())
                .build();

        StaffComplaint saved = complaintRepository.save(complaint);
        log.info("Complaint #{} submitted by {} ({})", saved.getId(),
                currentUser.getStaffId(), currentUser.getRole());

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
    // ADMIN: Get complaints — role-filtered
    //   HOD  → sees only HOD_LEVEL complaints (from LECTURERs, not yet escalated)
    //   DEAN → sees DEAN_LEVEL complaints (escalated) + HOD's own complaints
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ComplaintSummary> getAllComplaints(Status statusFilter, Pageable pageable) {
        StaffIdentity currentUser = getAuthenticatedUser();

        if (currentUser.getRole() == Roles.HOD) {
            // HOD sees only complaints still at HOD level (LECTURER submissions, not escalated)
            if (statusFilter != null) {
                return complaintRepository
                        .findByEscalationLevelAndStatus(EscalationLevel.HOD_LEVEL, statusFilter, pageable)
                        .map(this::toSummary);
            }
            return complaintRepository
                    .findByEscalationLevel(EscalationLevel.HOD_LEVEL, pageable)
                    .map(this::toSummary);

        } else if (currentUser.getRole() == Roles.DEAN) {
            // DEAN sees escalated complaints + HOD's own complaints
            if (statusFilter != null) {
                return complaintRepository
                        .findComplaintsVisibleToDeanByStatus(statusFilter, pageable)
                        .map(this::toSummary);
            }
            return complaintRepository
                    .findComplaintsVisibleToDean(pageable)
                    .map(this::toSummary);
        }

        throw new SecurityException("Your role does not have permission to view complaints");
    }

    // ─────────────────────────────────────────────────────────
    // ADMIN: Get single complaint by ID — with role visibility check
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id) {
        StaffIdentity currentUser = getAuthenticatedUser();
        StaffComplaint complaint = findById(id);
        assertCanView(currentUser, complaint);
        return toResponse(complaint);
    }

    // ─────────────────────────────────────────────────────────
    // HOD: Escalate a complaint up to Dean level
    // ─────────────────────────────────────────────────────────
    @Transactional
    public ComplaintResponse escalateComplaint(EscalateComplaintRequest request) {
        StaffIdentity hod = getAuthenticatedUser();

        if (hod.getRole() != Roles.HOD) {
            throw new SecurityException("Only HODs can escalate complaints to Dean level");
        }

        StaffComplaint complaint = findById(request.getId());

        // Guard: only HOD_LEVEL complaints can be escalated
        if (complaint.getEscalationLevel() == EscalationLevel.DEAN_LEVEL) {
            throw new RuntimeException("Complaint #" + request.getId() + " has already been escalated to Dean level");
        }

        // Guard: HOD can only escalate complaints they can see (HOD_LEVEL / LECTURER complaints)
        if (complaint.getEscalationLevel() != EscalationLevel.HOD_LEVEL) {
            throw new SecurityException("You do not have access to this complaint");
        }

        complaint.setEscalationLevel(EscalationLevel.DEAN_LEVEL);
        complaint.setEscalatedByStaffId(hod.getStaffId());
        complaint.setEscalatedByName(hod.getStaffFirstName() + " " + hod.getStaffLastName());
        complaint.setEscalatedAt(LocalDateTime.now());
        complaint.setEscalationNote(request.getEscalationNote());

        StaffComplaint updated = complaintRepository.save(complaint);
        log.info("Complaint #{} escalated to DEAN level by HOD {}", updated.getId(), hod.getStaffId());

        // Notify all Deans by event
        eventPublisher.publishEvent(new ComplaintEscalatedEvents(
                this, updated,
                hod.getStaffFirstName() + " " + hod.getStaffLastName(),
                request.getEscalationNote()
        ));

        return toResponse(updated);
    }

    // ─────────────────────────────────────────────────────────
    // ADMIN: Update complaint — with role visibility check
    // ─────────────────────────────────────────────────────────
    @Transactional
    public ComplaintResponse updateComplaint(Long id, UpdateComplaintRequest request) {
        StaffIdentity admin = getAuthenticatedUser();
        StaffComplaint complaint = findById(id);

        // Enforce that the admin can only update complaints they can see
        assertCanView(admin, complaint);

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

        boolean isNewAssignment = false;
        if (request.getAssignedManagerId() != null && !request.getAssignedManagerId().isBlank()) {
            isNewAssignment = !request.getAssignedManagerId().equals(complaint.getAssignedManagerId());
            complaint.setAssignedManagerId(request.getAssignedManagerId());
            complaint.setAssignedManagerName(request.getAssignedManagerName());
        }

        StaffComplaint updated = complaintRepository.save(complaint);
        log.info("Complaint #{} updated by {} ({})", id, admin.getStaffId(), admin.getRole());

        if (request.getStatus() != null && !previousStatus.equals(request.getStatus())) {
            eventPublisher.publishEvent(new ComplaintStatusUpdatedEvents(
                    this, updated, previousStatus,
                    complaint.getSubmittedByEmail(),
                    complaint.getSubmittedByName()
            ));
        }

        if (isNewAssignment) {
            staffIdentityRepo.findByStaffId(request.getAssignedManagerId())
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
    // ADMIN: Delete a complaint — with role visibility check
    // ─────────────────────────────────────────────────────────
    @Transactional
    public void deleteComplaint(Long id) {
        StaffIdentity currentUser = getAuthenticatedUser();
        StaffComplaint complaint = findById(id);
        assertCanView(currentUser, complaint);
        complaintRepository.delete(complaint);
        log.info("Complaint #{} deleted by {}", id, currentUser.getStaffId());
    }

    // ─────────────────────────────────────────────────────────
    // ADMIN: Search — role-filtered
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ComplaintSummary> searchComplaints(String keyword, Pageable pageable) {
        StaffIdentity currentUser = getAuthenticatedUser();

        if (currentUser.getRole() == Roles.HOD) {
            return complaintRepository
                    .searchByTitleForHod(keyword, EscalationLevel.HOD_LEVEL, pageable)
                    .map(this::toSummary);
        } else if (currentUser.getRole() == Roles.DEAN) {
            return complaintRepository
                    .searchByTitleForDean(keyword, pageable)
                    .map(this::toSummary);
        }

        throw new SecurityException("Your role does not have search access");
    }

    // ─────────────────────────────────────────────────────────
    // DASHBOARD STATS — role-aware
    // ─────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Map<String, Long> getDashboardStats() {
        StaffIdentity currentUser = getAuthenticatedUser();
        Map<String, Long> stats = new HashMap<>();

        if (currentUser.getRole() == Roles.HOD) {
            // HOD stats: only HOD_LEVEL complaints
            stats.put("total", complaintRepository.countByEscalationLevel(EscalationLevel.HOD_LEVEL));
            stats.put("open", complaintRepository.findByEscalationLevelAndStatus(
                    EscalationLevel.HOD_LEVEL, Status.OPEN,
                    org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements());
            stats.put("inProgress", complaintRepository.findByEscalationLevelAndStatus(
                    EscalationLevel.HOD_LEVEL, Status.IN_PROGRESS,
                    org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements());
            stats.put("resolved", complaintRepository.findByEscalationLevelAndStatus(
                    EscalationLevel.HOD_LEVEL, Status.RESOLVED,
                    org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements());
            stats.put("escalated", complaintRepository.countByEscalationLevel(EscalationLevel.DEAN_LEVEL));

        } else {
            // DEAN stats: DEAN_LEVEL + HOD submissions
            stats.put("total", complaintRepository.findComplaintsVisibleToDean(
                    org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements());
            stats.put("open", complaintRepository.findComplaintsVisibleToDeanByStatus(
                    Status.OPEN, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements());
            stats.put("inProgress", complaintRepository.findComplaintsVisibleToDeanByStatus(
                    Status.IN_PROGRESS, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements());
            stats.put("resolved", complaintRepository.findComplaintsVisibleToDeanByStatus(
                    Status.RESOLVED, org.springframework.data.domain.PageRequest.of(0, 1)).getTotalElements());
        }

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
        if (principal instanceof StaffIdentity staffIdentity) {
            return staffIdentity;
        }
        throw new EntityNotFoundException("Authenticated user not found in security context");
    }

    /**
     * Checks whether the current admin role is allowed to view/act on a complaint.
     *   HOD  → can only see HOD_LEVEL complaints
     *   DEAN → can only see DEAN_LEVEL complaints OR complaints submitted by HODs
     */
    private void assertCanView(StaffIdentity user, StaffComplaint complaint) {
        boolean canView = switch (user.getRole()) {
            case HOD  -> complaint.getEscalationLevel() == EscalationLevel.HOD_LEVEL;
            case DEAN -> complaint.getEscalationLevel() == EscalationLevel.DEAN_LEVEL
                    || "HOD".equals(complaint.getSubmittedByRole());
            default   -> false;
        };

        if (!canView) {
            throw new SecurityException(
                    "Access denied: you do not have permission to view complaint #" + complaint.getId());
        }
    }

    private ComplaintResponse toResponse(StaffComplaint c) {
        return ComplaintResponse.builder()
                .id(c.getId())
                .title(c.getTitle())
                .description(c.getDescription())
                .category(c.getCategory())
                .priority(c.getPriority())
                .status(c.getStatus())
                .escalationLevel(c.getEscalationLevel())
                .escalatedByName(c.getEscalatedByName())
                .escalatedAt(c.getEscalatedAt())
                .escalationNote(c.getEscalationNote())
                .adminResponse(c.getAdminResponse())
                .handledById(c.getHandledById())
                .handledByName(c.getHandledByName())
                .submittedByStaffId(c.getSubmittedByStaffId())
                .submittedByName(c.getSubmittedByName())
                .submittedByEmail(c.getSubmittedByEmail())
                .submittedByRole(c.getSubmittedByRole())
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
                .escalationLevel(c.getEscalationLevel())
                .submittedByName(c.getSubmittedByName())
                .submittedByRole(c.getSubmittedByRole())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
