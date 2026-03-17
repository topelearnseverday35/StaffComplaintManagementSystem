package FinalYearProject.StaffComplaintMgmtSystem.Exceptions;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Invalid Login credentials");
    }
}