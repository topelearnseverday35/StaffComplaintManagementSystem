package FinalYearProject.StaffComplaintMgmtSystem.service;

import FinalYearProject.StaffComplaintMgmtSystem.dto.*;
import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffComplaint;
import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffIdentity;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Department;
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
import org.springframework.data.domain.PageRequest;
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

    // ─── Submit ───────────────────────────────────────────────────────────
    @Transactional
    public ComplaintResponse submitComplaint(SubmitComplaintRequest request) {
        StaffIdentity user = getAuthenticatedUser();
        Department dept   = user.getDepartment();
        String school     = dept != null ? dept.getSchool() : null;

        EscalationLevel level = (user.getRole() == Roles.HOD)
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
                .escalationLevel(level)
                .submittedByStaffId(user.getStaffId())
                .submittedByName(user.getStaffFirstName() + " " + user.getStaffLastName())
                .submittedByEmail(user.getStaffEmail())
                .submittedByRole(user.getRole().name())
                .submittedByDepartment(dept)
                .submittedBySchool(school)
                .build();

        StaffComplaint saved = complaintRepository.save(complaint);
        log.info("Complaint #{} submitted by {} ({}) dept={}", saved.getId(),
                user.getStaffId(), user.getRole(), dept);

        eventPublisher.publishEvent(new ComplaintSubmittedEvents(
                this, saved, user.getStaffEmail(),
                user.getStaffFirstName() + " " + user.getStaffLastName()
        ));
        return toResponse(saved);
    }

    // ─── My complaints ────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ComplaintSummary> getMyComplaints(Pageable pageable) {
        StaffIdentity user = getAuthenticatedUser();
        return complaintRepository.findBySubmittedByStaffId(user.getStaffId(), pageable)
                .map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getMyComplaintById(Long id) {
        StaffIdentity user = getAuthenticatedUser();
        StaffComplaint c   = findById(id);
        if (!c.getSubmittedByStaffId().equals(user.getStaffId()))
            throw new SecurityException("You do not have access to this complaint");
        return toResponse(c);
    }

    // ─── Admin: list ──────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ComplaintSummary> getAllComplaints(Status statusFilter, Pageable pageable) {
        StaffIdentity user = getAuthenticatedUser();
        requireDepartment(user);

        if (user.getRole() == Roles.HOD) {
            // HOD sees HOD_LEVEL complaints only from their own department
            return (statusFilter != null)
                    ? complaintRepository.findByEscalationLevelAndStatusAndSubmittedByDepartment(
                            EscalationLevel.HOD_LEVEL, statusFilter, user.getDepartment(), pageable)
                    .map(this::toSummary)
                    : complaintRepository.findByEscalationLevelAndSubmittedByDepartment(
                            EscalationLevel.HOD_LEVEL, user.getDepartment(), pageable)
                    .map(this::toSummary);

        } else if (user.getRole() == Roles.DEAN) {
            // DEAN sees DEAN_LEVEL + HOD-submitted within their school
            String school = user.getDepartment().getSchool();
            return (statusFilter != null)
                    ? complaintRepository.findComplaintsVisibleToDeanByStatus(school, statusFilter, pageable)
                    .map(this::toSummary)
                    : complaintRepository.findComplaintsVisibleToDean(school, pageable)
                    .map(this::toSummary);
        }

        throw new SecurityException("Your role does not have permission to view complaints");
    }

    // ─── Admin: single ────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long id) {
        StaffIdentity user = getAuthenticatedUser();
        StaffComplaint c   = findById(id);
        assertCanView(user, c);
        return toResponse(c);
    }

    // ─── HOD: escalate ────────────────────────────────────────────────────
    @Transactional
    public ComplaintResponse escalateComplaint(EscalateComplaintRequest request) {
        StaffIdentity hod = getAuthenticatedUser();
        if (hod.getRole() != Roles.HOD)
            throw new SecurityException("Only HODs can escalate complaints to Dean level");

        StaffComplaint c = findById(request.getId());
        assertCanView(hod, c);   // ensures it belongs to their dept

        if (c.getEscalationLevel() == EscalationLevel.DEAN_LEVEL)
            throw new RuntimeException("Complaint #" + request.getId() + " is already escalated");

        c.setEscalationLevel(EscalationLevel.DEAN_LEVEL);
        c.setEscalatedByStaffId(hod.getStaffId());
        c.setEscalatedByName(hod.getStaffFirstName() + " " + hod.getStaffLastName());
        c.setEscalatedAt(LocalDateTime.now());
        c.setEscalationNote(request.getEscalationNote());

        StaffComplaint updated = complaintRepository.save(c);
        log.info("Complaint #{} escalated by HOD {} (dept={})", updated.getId(),
                hod.getStaffId(), hod.getDepartment());

        eventPublisher.publishEvent(new ComplaintEscalatedEvents(
                this, updated,
                hod.getStaffFirstName() + " " + hod.getStaffLastName(),
                request.getEscalationNote()
        ));
        return toResponse(updated);
    }

    // ─── Admin: update ────────────────────────────────────────────────────
    @Transactional
    public ComplaintResponse updateComplaint(Long id, UpdateComplaintRequest request) {
        StaffIdentity admin = getAuthenticatedUser();
        StaffComplaint c    = findById(id);
        assertCanView(admin, c);

        Status previousStatus = c.getStatus();

        if (request.getStatus() != null) {
            c.setStatus(request.getStatus());
            if (request.getStatus() == Status.RESOLVED) c.setResolvedAt(LocalDateTime.now());
        }
        if (request.getAdminResponse() != null) c.setAdminResponse(request.getAdminResponse());

        c.setHandledById(admin.getStaffId());
        c.setHandledByName(admin.getStaffFirstName() + " " + admin.getStaffLastName());

        boolean isNewAssignment = false;
        if (request.getAssignedManagerId() != null && !request.getAssignedManagerId().isBlank()) {
            isNewAssignment = !request.getAssignedManagerId().equals(c.getAssignedManagerId());
            c.setAssignedManagerId(request.getAssignedManagerId());
            c.setAssignedManagerName(request.getAssignedManagerName());
        }

        StaffComplaint updated = complaintRepository.save(c);

        if (request.getStatus() != null && !previousStatus.equals(request.getStatus())) {
            eventPublisher.publishEvent(new ComplaintStatusUpdatedEvents(
                    this, updated, previousStatus,
                    c.getSubmittedByEmail(), c.getSubmittedByName()));
        }

        if (isNewAssignment) {
            staffIdentityRepo.findByStaffId(request.getAssignedManagerId())
                    .ifPresent(mgr -> eventPublisher.publishEvent(new ComplaintAssignedEvents(
                            this, updated,
                            mgr.getStaffEmail(),
                            mgr.getStaffFirstName() + " " + mgr.getStaffLastName(),
                            admin.getStaffFirstName() + " " + admin.getStaffLastName())));
        }
        return toResponse(updated);
    }

    // ─── Admin: delete ────────────────────────────────────────────────────
    @Transactional
    public void deleteComplaint(Long id) {
        StaffIdentity user = getAuthenticatedUser();
        StaffComplaint c   = findById(id);
        assertCanView(user, c);
        complaintRepository.delete(c);
        log.info("Complaint #{} deleted by {}", id, user.getStaffId());
    }

    // ─── Admin: search ────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Page<ComplaintSummary> searchComplaints(String keyword, Pageable pageable) {
        StaffIdentity user = getAuthenticatedUser();
        requireDepartment(user);

        if (user.getRole() == Roles.HOD)
            return complaintRepository.searchByTitleForHod(keyword, user.getDepartment(), pageable)
                    .map(this::toSummary);

        if (user.getRole() == Roles.DEAN)
            return complaintRepository.searchByTitleForDean(
                            keyword, user.getDepartment().getSchool(), pageable)
                    .map(this::toSummary);

        throw new SecurityException("Your role does not have search access");
    }

    // ─── Stats ────────────────────────────────────────────────────────────
    @Transactional(readOnly = true)
    public Map<String, Long> getDashboardStats() {
        StaffIdentity user = getAuthenticatedUser();
        requireDepartment(user);
        Map<String, Long> stats = new HashMap<>();

        if (user.getRole() == Roles.HOD) {
            Department dept = user.getDepartment();
            stats.put("total",     complaintRepository.countByEscalationLevelAndDepartment(EscalationLevel.HOD_LEVEL, dept));
            stats.put("open",      complaintRepository.countByStatusAndDepartment(Status.OPEN, dept));
            stats.put("inProgress",complaintRepository.countByStatusAndDepartment(Status.IN_PROGRESS, dept));
            stats.put("resolved",  complaintRepository.countByStatusAndDepartment(Status.RESOLVED, dept));
            stats.put("escalated", complaintRepository.countByEscalationLevelAndDepartment(EscalationLevel.DEAN_LEVEL, dept));
        } else {
            String school = user.getDepartment().getSchool();
            stats.put("total",     complaintRepository.countVisibleToDean(school));
            stats.put("open",      complaintRepository.countVisibleToDeanByStatus(school, Status.OPEN));
            stats.put("inProgress",complaintRepository.countVisibleToDeanByStatus(school, Status.IN_PROGRESS));
            stats.put("resolved",  complaintRepository.countVisibleToDeanByStatus(school, Status.RESOLVED));
        }
        return stats;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────

    private StaffComplaint findById(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Complaint not found: " + id));
    }

    private StaffIdentity getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof StaffIdentity s) return s;
        throw new EntityNotFoundException("Authenticated user not found in security context");
    }

    private void requireDepartment(StaffIdentity user) {
        if (user.getDepartment() == null)
            throw new RuntimeException("Your account has no department assigned. Please contact admin.");
    }

    /**
     * HOD  → may only access HOD_LEVEL complaints in their own department.
     * DEAN → may only access DEAN_LEVEL or HOD-submitted complaints in their own school.
     */
    private void assertCanView(StaffIdentity user, StaffComplaint c) {
        requireDepartment(user);
        boolean canView = switch (user.getRole()) {
            case HOD  -> c.getEscalationLevel() == EscalationLevel.HOD_LEVEL
                    && user.getDepartment().equals(c.getSubmittedByDepartment());
            case DEAN -> (c.getEscalationLevel() == EscalationLevel.DEAN_LEVEL
                    || "HOD".equals(c.getSubmittedByRole()))
                    && user.getDepartment().getSchool().equals(c.getSubmittedBySchool());
            default   -> false;
        };
        if (!canView)
            throw new SecurityException(
                    "Access denied: complaint #" + c.getId() + " is outside your department/school scope");
    }

    private ComplaintResponse toResponse(StaffComplaint c) {
        return ComplaintResponse.builder()
                .id(c.getId()).title(c.getTitle()).description(c.getDescription())
                .category(c.getCategory()).priority(c.getPriority()).status(c.getStatus())
                .escalationLevel(c.getEscalationLevel()).escalatedByName(c.getEscalatedByName())
                .escalatedAt(c.getEscalatedAt()).escalationNote(c.getEscalationNote())
                .adminResponse(c.getAdminResponse()).handledById(c.getHandledById())
                .handledByName(c.getHandledByName()).submittedByStaffId(c.getSubmittedByStaffId())
                .submittedByName(c.getSubmittedByName()).submittedByEmail(c.getSubmittedByEmail())
                .submittedByRole(c.getSubmittedByRole()).submittedByDepartment(c.getSubmittedByDepartment())
                .submittedBySchool(c.getSubmittedBySchool())
                .createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt()).resolvedAt(c.getResolvedAt())
                .build();
    }

    private ComplaintSummary toSummary(StaffComplaint c) {
        return ComplaintSummary.builder()
                .id(c.getId()).title(c.getTitle()).category(c.getCategory())
                .priority(c.getPriority()).status(c.getStatus())
                .escalationLevel(c.getEscalationLevel()).submittedByName(c.getSubmittedByName())
                .submittedByRole(c.getSubmittedByRole()).submittedByDepartment(c.getSubmittedByDepartment())
                .submittedBySchool(c.getSubmittedBySchool()).createdAt(c.getCreatedAt())
                .build();
    }
}
