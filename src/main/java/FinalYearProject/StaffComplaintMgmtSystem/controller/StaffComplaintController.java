package FinalYearProject.StaffComplaintMgmtSystem.controller;


import FinalYearProject.StaffComplaintMgmtSystem.dto.*;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Status;
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

    @PostMapping("/auth/sign-up")
    public GeneralResponse signUp(@RequestBody Registrationrequest request) {
        log.info("Sign up has been called:::::");
        return registration.registration(request);
    }

    @GetMapping("/auth/getAllStaff")
    public GeneralResponse getAllStaff() {
        log.info("getAllStaff has been called :::::");
        return registration.getAllStaff();
    }

    @PostMapping("/auth/update-staff")
    public GeneralResponse updateStaffIdentity(@RequestBody UpdateStaffRequest request) {
        log.info("Update Staff has been called:::::");
        return registration.updateStaffIdentity(request);
    }

    @DeleteMapping("/auth/delete-staff")
    public GeneralResponse deleteStaffIdentity(@RequestBody DeleteStaffRequest request) {
        log.info("DeleteStaff has been called:::::");
        return registration.deleteStaffIdentity(request);
    }

    @PostMapping("/auth/log-in")
    public ResponseEntity<AuthenticationResponse> logIn(@RequestBody LoginRequest logInRequest) {
        log.info("log-in has been called::::::");
        return registration.LogIn(logInRequest);
    }

    @PostMapping("/auth/log-out")
    public ResponseEntity<GeneralResponse> logout(HttpServletResponse response) {
        log.info("log-out has been called::::::");
        return registration.LogOut(response);

    }

    @PreAuthorize("hasAnyRole('LECTURER', 'HOD', 'DEAN')")
    @PostMapping("/submitComplaint")
    public ResponseEntity<ComplaintResponse> submitComplaint(
            @Valid @RequestBody SubmitComplaintRequest request) {;
        log.info("Complaint submission request received");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(complaintService.submitComplaint(request));
    }

    /**
     * GET /complaints/my
     * Frontend: portal.html → loadMyComplaints(), loadHomeRecent()
     * Staff views their own complaints (paginated).
     */
    @GetMapping("/getMyComplaints")
    @PreAuthorize("hasAnyRole('LECTURER', 'HOD', 'DEAN')")
    public ResponseEntity<Page<ComplaintSummary>> getMyComplaints(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(complaintService.getMyComplaints(pageable));
    }

    /**
     * POST /complaints/my/detail
     * Frontend: portal.html → openDetailModal()
     * Staff views a specific complaint they submitted.
     * Body: { "id": 1 }
     */
    @PostMapping("/my/detail")
    public ResponseEntity<ComplaintResponse> getMyComplaintById(@RequestBody IdRequest request) {
        return ResponseEntity.ok(complaintService.getMyComplaintById(request.getId()));
    }

    // ═══════════════════════════════════════════════════════════
    // ADMIN ENDPOINTS  (role: DEAN or HOD)
    // ═══════════════════════════════════════════════════════════

    /**
     * GET /complaints/admin/all
     * Frontend: dashboard.html → loadComplaints(), loadRecentComplaints()
     * Admin views all complaints with optional status filter.
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasAuthority('DEAN') or hasAuthority('HOD')")
    public ResponseEntity<Page<ComplaintSummary>> getAllComplaints(
            @RequestParam(required = false) Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(complaintService.getAllComplaints(status, pageable));
    }

    /**
     * POST /complaints/admin/detail
     * Frontend: dashboard.html → openModal()
     * Admin views full details of any single complaint.
     * Body: { "id": 1 }
     */
    @PostMapping("/admin/detail")
    @PreAuthorize("hasAuthority('DEAN') or hasAuthority('HOD')")
    public ResponseEntity<ComplaintResponse> getComplaintById(
            @RequestBody IdRequest request) {
        return ResponseEntity.ok(complaintService.getComplaintById(request.getId()));
    }

    /**
     * PUT /complaints/admin/update
     * Frontend: dashboard.html → submitUpdate()
     * Admin updates status, adds response, or assigns to manager.
     * Body: { "id": 1, "status": "IN_PROGRESS", "adminResponse": "..." }
     */
    @PutMapping("/admin/update")
    @PreAuthorize("hasAuthority('DEAN') or hasAuthority('HOD')")
    public ResponseEntity<ComplaintResponse> updateComplaint(
            @RequestBody UpdateComplaintRequest request) {
        log.info("Admin updating complaint #{}", request.getId());
        return ResponseEntity.ok(
                complaintService.updateComplaint(request.getId(), request));
    }

    /**
     * DELETE /complaints/admin/delete
     * Frontend: dashboard.html → deleteComplaint()
     * Admin deletes a complaint.
     * Body: { "id": 1 }
     */
    @DeleteMapping("/admin/delete")
    @PreAuthorize("hasAuthority('DEAN') or hasAuthority('HOD')")
    public ResponseEntity<Map<String, String>> deleteComplaint(
            @RequestBody IdRequest request) {
        complaintService.deleteComplaint(request.getId());
        return ResponseEntity.ok(
                Map.of("message", "Complaint #" + request.getId()+ " has been deleted"));
    }

    /**
     * POST /complaints/admin/search
     * Frontend: dashboard.html → loadComplaints() with keyword
     * Admin searches complaints by title keyword.
     * Body: { "keyword": "harassment" }
     */
    @PostMapping("/admin/search")
    @PreAuthorize("hasAnyAuthority('DEAN', 'HOD')")
    public ResponseEntity<Page<ComplaintSummary>> searchComplaints(
            @RequestBody SearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(complaintService.searchComplaints(request.getKeyword(), pageable));
    }
    /**
     * GET /complaints/admin/stats
     * Frontend: dashboard.html → loadStats()
     * Admin dashboard statistics — no body needed.
     */
    @GetMapping("/admin/stats")
    @PreAuthorize("hasAuthority('DEAN') or hasAuthority('HOD')")
    public ResponseEntity<Map<String, Long>> getDashboardStats() {
        return ResponseEntity.ok(complaintService.getDashboardStats());
    }
}