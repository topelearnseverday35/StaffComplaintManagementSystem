package FinalYearProject.StaffComplaintMgmtSystem.events;

import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffIdentity;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Roles;
import FinalYearProject.StaffComplaintMgmtSystem.repository.StaffIdentityRepo;
import FinalYearProject.StaffComplaintMgmtSystem.utils.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ComplaintEventListener {

    private final EmailService emailService;
    private final StaffIdentityRepo staffIdentityRepo;

    /** When a complaint is submitted: notify the submitter + relevant admins */
    @Async
    @EventListener
    public void onComplaintSubmitted(ComplaintSubmittedEvents event) {
        log.info("Handling ComplaintSubmittedEvent for complaint #{}", event.getComplaint().getId());

        // Notify the submitting staff member
        String staffHtml = emailService.buildComplaintSubmittedStaffEmail(
                event.getStaffName(),
                event.getComplaint().getTitle(),
                event.getComplaint().getId()
        );
        emailService.sendHtmlEmail(event.getStaffEmail(), "Your Complaint Has Been Received", staffHtml);

        // Determine which admins to notify based on escalation level
        // HOD_LEVEL complaints → notify HODs
        // DEAN_LEVEL complaints (HOD submissions) → notify DEANs
        Roles targetRole = switch (event.getComplaint().getEscalationLevel()) {
            case HOD_LEVEL  -> Roles.HOD;
            case DEAN_LEVEL -> Roles.DEAN;
        };

        List<StaffIdentity> admins = staffIdentityRepo.findAll().stream()
                .filter(s -> s.getRole() == targetRole)
                .toList();

        for (StaffIdentity admin : admins) {
            String adminHtml = emailService.buildComplaintSubmittedAdminEmail(
                    admin.getStaffFirstName() + " " + admin.getStaffLastName(),
                    event.getStaffName(),
                    event.getComplaint().getTitle(),
                    event.getComplaint().getCategory().name(),
                    event.getComplaint().getPriority().name(),
                    event.getComplaint().getId()
            );
            emailService.sendHtmlEmail(admin.getStaffEmail(), "New Complaint Submitted", adminHtml);
        }
    }

    /** When a complaint status changes: notify the original submitter */
    @Async
    @EventListener
    public void onStatusUpdated(ComplaintStatusUpdatedEvents event) {
        log.info("Handling ComplaintStatusUpdatedEvent for complaint #{}", event.getComplaint().getId());

        String html = emailService.buildStatusUpdateEmail(
                event.getStaffName(),
                event.getComplaint().getTitle(),
                event.getPreviousStatus().name(),
                event.getComplaint().getStatus().name(),
                event.getComplaint().getAdminResponse(),
                event.getComplaint().getId()
        );
        emailService.sendHtmlEmail(event.getStaffEmail(), "Your Complaint Status Has Been Updated", html);
    }

    /** When a complaint is assigned to a manager: notify that manager */
    @Async
    @EventListener
    public void onComplaintAssigned(ComplaintAssignedEvents event) {
        log.info("Handling ComplaintAssignedEvent for complaint #{}", event.getComplaint().getId());

        String html = emailService.buildComplaintAssignedEmail(
                event.getManagerName(),
                event.getAssignedByName(),
                event.getComplaint().getTitle(),
                event.getComplaint().getSubmittedByName(),
                event.getComplaint().getCategory().name(),
                event.getComplaint().getPriority().name(),
                event.getComplaint().getId()
        );
        emailService.sendHtmlEmail(event.getManagerEmail(), "A Complaint Has Been Assigned to You", html);
    }

    /** When HOD escalates a complaint: notify all Deans */
    @Async
    @EventListener
    public void onComplaintEscalated(ComplaintEscalatedEvents event) {
        log.info("Handling ComplaintEscalatedEvent for complaint #{}", event.getComplaint().getId());

        List<StaffIdentity> deans = staffIdentityRepo.findAll().stream()
                .filter(s -> s.getRole() == Roles.DEAN)
                .toList();

        for (StaffIdentity dean : deans) {
            String html = emailService.buildComplaintEscalatedEmail(
                    dean.getStaffFirstName() + " " + dean.getStaffLastName(),
                    event.getHodName(),
                    event.getComplaint().getTitle(),
                    event.getComplaint().getSubmittedByName(),
                    event.getComplaint().getCategory().name(),
                    event.getComplaint().getPriority().name(),
                    event.getEscalationNote(),
                    event.getComplaint().getId()
            );
            emailService.sendHtmlEmail(dean.getStaffEmail(), "Complaint Escalated to Your Attention", html);
        }
    }
}
