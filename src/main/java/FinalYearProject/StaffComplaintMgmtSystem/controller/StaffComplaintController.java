package FinalYearProject.StaffComplaintMgmtSystem.controller;

import FinalYearProject.StaffComplaintMgmtSystem.dto.*;
import FinalYearProject.StaffComplaintMgmtSystem.service.ComplaintService;
import FinalYearProject.StaffComplaintMgmtSystem.service.Registration;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/staffcomplaintmgmtsystem")
@RequiredArgsConstructor
@Slf4j
public class StaffComplaintController {

    private final Registration registration;
    private final ComplaintService complaintService;

    // ═══════════════════════════════════════════════════════════
    // AUTH ENDPOINTS
    // ═══════════════════════════════════════════════════════════

    @PostMapping("/auth/sign-up")
    public GeneralResponse signUp(@Valid @RequestBody Registrationrequest request) {
        log.info("Sign up has been called");
        return registration.registration(request);
    }

    @GetMapping("/auth/getAllStaff")
    public GeneralResponse getAllStaff() {
        log.info("getAllStaff has been called");
        return registration.getAllStaff();
    }

    @PostMapping("/auth/update-staff")
    public GeneralResponse updateStaffIdentity(@RequestBody UpdateStaffRequest request) {
        log.info("Update Staff has been called");
        return registration.updateStaffIdentity(request);
    }

    @DeleteMapping("/auth/delete-staff")
    public GeneralResponse deleteStaffIdentity(@RequestBody DeleteStaffRequest request) {
        log.info("DeleteStaff has been called");
        return registration.deleteStaffIdentity(request);
    }

    @PostMapping("/auth/log-in")
    public ResponseEntity<AuthenticationResponse> logIn(@RequestBody LoginRequest logInRequest) {
        log.info("log-in has been called");
        return registration.LogIn(logInRequest);
    }

    @PostMapping("/auth/log-out")
    public ResponseEntity<GeneralResponse> logout(HttpServletResponse response) {
        log.info("log-out has been called");
        return registration.LogOut(response);
    }

    // ═══════════════════════════════════════════════════════════
    // STAFF ENDPOINTS
    // ═══════════════════════════════════════════════════════════

    /**
     * POST /submitComplaint
     * LECTURER submits → visible to HOD only.
     * HOD submits → visible to DEAN only.
     * Body: { "title": "...", "description": "...", "category": "HR", "priority": "HIGH" }
     */
    @PreAuthorize("hasAnyRole('LECTURER', 'HOD', 'DEAN')")
    @PostMapping("/submitComplaint")
    public ResponseEntity<ComplaintResponse> submitComplaint(
            @Valid @RequestBody SubmitComplaintRequest request) {
        log.info("Complaint submission request received");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(complaintService.submitComplaint(request));
    }

    /**
     * POST /getMyComplaints
     * Any staff views their own submitted complaints (paginated).
     * Body: { "page": 0, "size": 10 }
     */
    @PostMapping("/getMyComplaints")
    @PreAuthorize("hasAnyRole('LECTURER', 'HOD', 'DEAN')")
    public ResponseEntity<Page<ComplaintSummary>> getMyComplaints(
            @RequestBody PageRequestDTO request) {
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10,
                Sort.by("createdAt").descending()
        );
        return ResponseEntity.ok(complaintService.getMyComplaints(pageable));
    }

    /**
     * POST /my/detail
     * Any staff views a specific complaint they submitted.
     * Body: { "id": 1 }
     */
    @PostMapping("/my/detail")
    @PreAuthorize("hasAnyRole('LECTURER', 'HOD', 'DEAN')")
    public ResponseEntity<ComplaintResponse> getMyComplaintById(@RequestBody IdRequest request) {
        return ResponseEntity.ok(complaintService.getMyComplaintById(request.getId()));
    }

    // ═══════════════════════════════════════════════════════════
    // HOD ENDPOINTS
    // ═══════════════════════════════════════════════════════════

    /**
     * POST /hod/escalate
     * HOD escalates a LECTURER's complaint up to Dean level.
     * Once escalated, only the Dean can see it — HOD loses visibility.
     * Body: { "id": 1, "escalationNote": "Requires Dean-level attention because..." }
     */
    @PostMapping("/hod/escalate")
    @PreAuthorize("hasAuthority('HOD')")
    public ResponseEntity<ComplaintResponse> escalateComplaint(
            @Valid @RequestBody EscalateComplaintRequest request) {
        log.info("HOD escalating complaint #{} to Dean", request.getId());
        return ResponseEntity.ok(complaintService.escalateComplaint(request));
    }

    // ═══════════════════════════════════════════════════════════
    // SHARED ADMIN ENDPOINTS (HOD and DEAN — each sees their own scope)
    // ═══════════════════════════════════════════════════════════

    /**
     * POST /admin/all
     * HOD  → sees only LECTURER complaints (HOD_LEVEL, not escalated).
     * DEAN → sees only escalated complaints + HOD's own complaints.
     * Body: { "status": "OPEN", "page": 0, "size": 10 }
     */
    @PostMapping("/admin/all")
    @PreAuthorize("hasAnyAuthority('DEAN', 'HOD')")
    public ResponseEntity<Page<ComplaintSummary>> getAllComplaints(
            @RequestBody AdminFilterRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10,
                Sort.by("createdAt").descending()
        );
        return ResponseEntity.ok(complaintService.getAllComplaints(request.getStatus(), pageable));
    }

    /**
     * POST /admin/detail
     * View full details of a complaint — access enforced by role.
     * Body: { "id": 1 }
     */
    @PostMapping("/admin/detail")
    @PreAuthorize("hasAnyAuthority('DEAN', 'HOD')")
    public ResponseEntity<ComplaintResponse> getComplaintById(@RequestBody IdRequest request) {
        return ResponseEntity.ok(complaintService.getComplaintById(request.getId()));
    }

    /**
     * PUT /admin/update
     * Update status, add response, or assign manager — access enforced by role.
     * Body: { "id": 1, "status": "IN_PROGRESS", "adminResponse": "..." }
     */
    @PutMapping("/admin/update")
    @PreAuthorize("hasAnyAuthority('DEAN', 'HOD')")
    public ResponseEntity<ComplaintResponse> updateComplaint(
            @RequestBody UpdateComplaintRequest request) {
        log.info("Admin updating complaint #{}", request.getId());
        return ResponseEntity.ok(complaintService.updateComplaint(request.getId(), request));
    }

    /**
     * DELETE /admin/delete
     * Delete a complaint — access enforced by role.
     * Body: { "id": 1 }
     */
    @DeleteMapping("/admin/delete")
    @PreAuthorize("hasAnyAuthority('DEAN', 'HOD')")
    public ResponseEntity<Map<String, String>> deleteComplaint(@RequestBody IdRequest request) {
        complaintService.deleteComplaint(request.getId());
        return ResponseEntity.ok(Map.of("message", "Complaint #" + request.getId() + " has been deleted"));
    }

    /**
     * POST /admin/search
     * Search complaints by keyword — each role searches within their own scope.
     * Body: { "keyword": "harassment", "page": 0, "size": 10 }
     */
    @PostMapping("/admin/search")
    @PreAuthorize("hasAnyAuthority('DEAN', 'HOD')")
    public ResponseEntity<Page<ComplaintSummary>> searchComplaints(
            @RequestBody SearchRequest request) {
        Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 10,
                Sort.by("createdAt").descending()
        );
        return ResponseEntity.ok(complaintService.searchComplaints(request.getKeyword(), pageable));
    }

    /**
     * GET /admin/stats
     * Dashboard statistics — scoped to the caller's role.
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasAnyAuthority('DEAN', 'HOD')")
    public ResponseEntity<Map<String, Long>> getDashboardStats() {
        return ResponseEntity.ok(complaintService.getDashboardStats());
    }
}
