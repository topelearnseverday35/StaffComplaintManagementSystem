package FinalYearProject.StaffComplaintMgmtSystem.utils;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Staff Complaint Management System}")
    private String appName;

    /**
     * Sends an HTML email asynchronously so it never blocks request threads.
     */
    @Async
    public void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("[" + appName + "] " + subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("Email sent successfully to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    // EMAIL TEMPLATES
    // ─────────────────────────────────────────────────────────

    /** Email sent to admins when a new complaint is submitted */
    public String buildComplaintSubmittedAdminEmail(String adminName, String staffName,
                                                    String complaintTitle, String category,
                                                    String priority, Long complaintId) {
        return baseTemplate(
                "New Complaint Submitted",
                "Hello " + adminName + ",",
                "<p>A new complaint has been submitted and requires your attention.</p>" +
                        detailsTable(new String[][]{
                                {"Complaint ID", "#" + complaintId},
                                {"Title", complaintTitle},
                                {"Submitted By", staffName},
                                {"Category", category},
                                {"Priority", badgeHtml(priority)},
                                {"Status", badgeHtml("OPEN")}
                        }) +
                        "<p>Please log in to the portal to review and respond.</p>",
                "View Complaint", "#"
        );
    }

    /** Email sent to staff confirming their complaint was received */
    public String buildComplaintSubmittedStaffEmail(String staffName, String complaintTitle,
                                                    Long complaintId) {
        return baseTemplate(
                "Complaint Received",
                "Hello " + staffName + ",",
                "<p>Your complaint has been successfully submitted. Our team will review it and get back to you shortly.</p>" +
                        detailsTable(new String[][]{
                                {"Complaint ID", "#" + complaintId},
                                {"Title", complaintTitle},
                                {"Status", badgeHtml("OPEN")}
                        }) +
                        "<p>You will receive a notification when your complaint status is updated.</p>",
                "Track Status", "#"
        );
    }

    /** Email sent to staff when their complaint status changes */
    public String buildStatusUpdateEmail(String staffName, String complaintTitle,
                                         String previousStatus, String newStatus,
                                         String adminResponse, Long complaintId) {
        String responseSection = (adminResponse != null && !adminResponse.isBlank())
                ? "<div style='background:#1e293b;border-left:4px solid #6366f1;padding:16px;margin:16px 0;border-radius:4px;'>" +
                "<strong style='color:#a5b4fc;'>Admin Response:</strong>" +
                "<p style='color:#cbd5e1;margin-top:8px;'>" + adminResponse + "</p></div>"
                : "";

        return baseTemplate(
                "Complaint Status Updated",
                "Hello " + staffName + ",",
                "<p>Your complaint status has been updated.</p>" +
                        detailsTable(new String[][]{
                                {"Complaint ID", "#" + complaintId},
                                {"Title", complaintTitle},
                                {"Previous Status", badgeHtml(previousStatus)},
                                {"New Status", badgeHtml(newStatus)}
                        }) +
                        responseSection,
                "View Details", "#"
        );
    }

    /** Email sent to manager when a complaint is assigned to them */
    public String buildComplaintAssignedEmail(String managerName, String assignedByName,
                                              String complaintTitle, String staffName,
                                              String category, String priority, Long complaintId) {
        return baseTemplate(
                "Complaint Assigned to You",
                "Hello " + managerName + ",",
                "<p>A complaint has been assigned to you by <strong>" + assignedByName + "</strong> for review and resolution.</p>" +
                        detailsTable(new String[][]{
                                {"Complaint ID", "#" + complaintId},
                                {"Title", complaintTitle},
                                {"Submitted By", staffName},
                                {"Category", category},
                                {"Priority", badgeHtml(priority)}
                        }) +
                        "<p>Please log in to the portal to review and take action.</p>",
                "View Complaint", "#"
        );
    }

    // ─────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────

    private String detailsTable(String[][] rows) {
        StringBuilder sb = new StringBuilder(
                "<table style='width:100%;border-collapse:collapse;margin:16px 0;'>"
        );
        for (String[] row : rows) {
            sb.append("<tr>")
                    .append("<td style='padding:10px;background:#1e293b;color:#94a3b8;font-weight:600;width:40%;border-bottom:1px solid #334155;'>")
                    .append(row[0]).append("</td>")
                    .append("<td style='padding:10px;background:#0f172a;color:#e2e8f0;border-bottom:1px solid #334155;'>")
                    .append(row[1]).append("</td>")
                    .append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private String badgeHtml(String value) {
        String color = switch (value.toUpperCase()) {
            case "HIGH", "OPEN"       -> "#ef4444";
            case "MEDIUM", "IN_PROGRESS" -> "#f59e0b";
            case "LOW", "RESOLVED"    -> "#22c55e";
            case "CLOSED"             -> "#6b7280";
            default                   -> "#6366f1";
        };
        return "<span style='background:" + color + "20;color:" + color +
                ";padding:3px 10px;border-radius:20px;font-size:12px;font-weight:700;'>" +
                value + "</span>";
    }

    private String baseTemplate(String heading, String greeting, String content, String btnText, String btnUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background:#0a0f1e;font-family:'Segoe UI',Tahoma,Geneva,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="padding:40px 20px;">
                <tr><td align="center">
                  <table width="600" style="background:#0f172a;border-radius:12px;overflow:hidden;border:1px solid #1e293b;">
                    <!-- Header -->
                    <tr><td style="background:linear-gradient(135deg,#1e1b4b,#312e81);padding:32px;text-align:center;">
                      <h1 style="color:#a5b4fc;margin:0;font-size:20px;letter-spacing:2px;text-transform:uppercase;">Staff Complaint Management</h1>
                      <p style="color:#6366f1;margin:8px 0 0;font-size:24px;font-weight:700;">%s</p>
                    </td></tr>
                    <!-- Body -->
                    <tr><td style="padding:32px;">
                      <p style="color:#cbd5e1;font-size:16px;margin-top:0;">%s</p>
                      <div style="color:#94a3b8;line-height:1.7;">%s</div>
                      <div style="text-align:center;margin:32px 0;">
                        <a href="%s" style="background:linear-gradient(135deg,#4f46e5,#7c3aed);color:white;padding:14px 32px;border-radius:8px;text-decoration:none;font-weight:700;font-size:15px;">%s</a>
                      </div>
                    </td></tr>
                    <!-- Footer -->
                    <tr><td style="background:#0a0f1e;padding:20px;text-align:center;border-top:1px solid #1e293b;">
                      <p style="color:#475569;font-size:12px;margin:0;">This is an automated notification. Please do not reply to this email.</p>
                    </td></tr>
                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(heading, greeting, content, btnUrl, btnText);
    }
}
