package FinalYearProject.StaffComplaintMgmtSystem.events;

import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffComplaint;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ComplaintSubmittedEvents extends ApplicationEvent {

    private final StaffComplaint complaint;
    private final String staffEmail;
    private final String staffName;

    public ComplaintSubmittedEvents(Object source, StaffComplaint complaint,
                                   String staffEmail, String staffName) {
        super(source);
        this.complaint = complaint;
        this.staffEmail = staffEmail;
        this.staffName = staffName;
    }
}