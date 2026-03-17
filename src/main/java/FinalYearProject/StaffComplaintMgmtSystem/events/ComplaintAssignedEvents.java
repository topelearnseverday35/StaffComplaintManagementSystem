package FinalYearProject.StaffComplaintMgmtSystem.events;


import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffComplaint;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Service;

/**
 * Fired when admin assigns a complaint to a manager.
 * Triggers email to the manager notifying them of the assignment.
 */
@Getter
public class ComplaintAssignedEvents extends ApplicationEvent {

    private final StaffComplaint complaint;
    private final String managerEmail;
    private final String managerName;
    private final String assignedByName;

    public ComplaintAssignedEvents(Object source, StaffComplaint complaint,
                                  String managerEmail, String managerName,
                                  String assignedByName) {
        super(source);
        this.complaint = complaint;
        this.managerEmail = managerEmail;
        this.managerName = managerName;
        this.assignedByName = assignedByName;
    }
}
