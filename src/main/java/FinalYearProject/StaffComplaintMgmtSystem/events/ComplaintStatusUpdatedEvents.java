package FinalYearProject.StaffComplaintMgmtSystem.events;

import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffComplaint;
import FinalYearProject.StaffComplaintMgmtSystem.enums.Status;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ComplaintStatusUpdatedEvents extends ApplicationEvent {

    private final StaffComplaint complaint;
    private final Status previousStatus;
    private final String staffEmail;
    private final String staffName;

    public ComplaintStatusUpdatedEvents(Object source, StaffComplaint complaint,
                                        Status previousStatus,
                                        String staffEmail, String staffName) {
        super(source);
        this.complaint = complaint;
        this.previousStatus = previousStatus;
        this.staffEmail = staffEmail;
        this.staffName = staffName;
    }
}
