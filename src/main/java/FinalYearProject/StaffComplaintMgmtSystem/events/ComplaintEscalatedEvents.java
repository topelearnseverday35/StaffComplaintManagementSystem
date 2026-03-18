package FinalYearProject.StaffComplaintMgmtSystem.events;

import FinalYearProject.StaffComplaintMgmtSystem.entities.StaffComplaint;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Fired when a HOD escalates a complaint to the Dean level.
 * Triggers notification email to all Deans.
 */
@Getter
public class ComplaintEscalatedEvents extends ApplicationEvent {

    private final StaffComplaint complaint;
    private final String hodName;
    private final String escalationNote;

    public ComplaintEscalatedEvents(Object source, StaffComplaint complaint,
                                    String hodName, String escalationNote) {
        super(source);
        this.complaint = complaint;
        this.hodName = hodName;
        this.escalationNote = escalationNote;
    }
}
