package FinalYearProject.StaffComplaintMgmtSystem.enums;

/**
 * Tracks who currently owns/can see this complaint.
 *
 * HOD_LEVEL  → default; submitted by LECTURER → only HOD can see it
 * DEAN_LEVEL → HOD escalated it up → only DEAN can see it
 *              (the original LECTURER complaint is now visible to DEAN)
 */
public enum EscalationLevel {
    HOD_LEVEL,
    DEAN_LEVEL
}
