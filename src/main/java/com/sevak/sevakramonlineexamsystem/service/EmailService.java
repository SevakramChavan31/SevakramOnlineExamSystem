package com.sevak.sevakramonlineexamsystem.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private final JavaMailSender gmailMailSender;
    private final JavaMailSender resendMailSender;
    private final ResendApiClient resendApiClient;

    @Value("${app.mail.from:}")
    private String gmailFromEmail;

    @Value("${app.mail.cc:}")
    private String ccEmail;

    @Value("${resend.smtp.from:}")
    private String resendFromEmail;

    public EmailService(
            @Qualifier("gmailMailSender") JavaMailSender gmailMailSender,
            @Qualifier("resendMailSender") JavaMailSender resendMailSender,
            ObjectProvider<ResendApiClient> resendApiClientProvider) {

        this.gmailMailSender = gmailMailSender;
        this.resendMailSender = resendMailSender;
        this.resendApiClient = resendApiClientProvider.getIfAvailable();
    }

    // =========================================================
    // FORGOT PASSWORD OTP
    // =========================================================

    public void sendOtpEmail(String studentEmail, String otp) {

        validateEmailAndOtp(studentEmail, otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(getGmailFromEmail());
        message.setTo(studentEmail.trim());

        message.setSubject(
                "Sevakram Online Examination System - Password Reset OTP"
        );

        message.setText(
                "Hello,\n\n"
                        + "We received a request to reset your password "
                        + "for the Sevakram Online Examination System.\n\n"
                        + "Your password reset OTP is:\n\n"
                        + otp.trim()
                        + "\n\n"
                        + "This OTP is valid for 10 minutes.\n\n"
                        + "For your security, do not share this code with anyone.\n\n"
                        + "If you did not request a password reset, "
                        + "you can safely ignore this email. "
                        + "Your password will remain unchanged.\n\n"
                        + "Regards,\n"
                        + "Sevakram Chavan\n"
                        + "Sevakram Online Examination System"
        );

        gmailMailSender.send(message);
    }

    // =========================================================
    // ADMIN LOGIN OTP
    // =========================================================

    public void sendLoginOtpEmail(String adminEmail, String otp) {

        validateEmailAndOtp(adminEmail, otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(getGmailFromEmail());
        message.setTo(adminEmail.trim());

        message.setSubject(
                "Sevakram Online Examination System - Admin Login Verification"
        );

        message.setText(
                "Dear Administrator,\n\n"
                        + "A verification code has been requested "
                        + "to sign in to your Admin account.\n\n"
                        + "Your Admin Login OTP is:\n\n"
                        + otp.trim()
                        + "\n\n"
                        + "This OTP is valid for 5 minutes.\n\n"
                        + "Enter this code on the Admin Login verification page "
                        + "to complete the sign-in process.\n\n"
                        + "For your security, do not share this code with anyone.\n\n"
                        + "If you did not attempt to sign in, "
                        + "please ignore this email.\n\n"
                        + "Regards,\n"
                        + "Sevakram Chavan\n"
                        + "Sevakram Online Examination System"
        );

        gmailMailSender.send(message);
    }

    // =========================================================
    // STUDENT LOGIN OTP
    // =========================================================

    public void sendStudentLoginOtpEmail(
            String studentEmail,
            String otp) {

        validateEmailAndOtp(studentEmail, otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(getGmailFromEmail());
        message.setTo(studentEmail.trim());

        message.setSubject(
                "Sevakram Online Examination System - Student Login Verification"
        );

        message.setText(
                "Dear Student,\n\n"
                        + "A sign-in verification code has been requested "
                        + "for your Student account.\n\n"
                        + "Your Student Login OTP is:\n\n"
                        + otp.trim()
                        + "\n\n"
                        + "This OTP is valid for 5 minutes.\n\n"
                        + "Enter this code on the Student Login verification page "
                        + "to complete your sign-in.\n\n"
                        + "For your security, do not share this code with anyone.\n\n"
                        + "If you did not attempt to sign in, "
                        + "please ignore this email.\n\n"
                        + "Regards,\n"
                        + "Sevakram Chavan\n"
                        + "Sevakram Online Examination System"
        );

        gmailMailSender.send(message);
    }

    // =========================================================
    // SUPPORT TICKET REPLY EMAIL
    // Professional Blue + White HTML email.
    // =========================================================

    public void sendSupportTicketReplyEmail(
            String studentEmail,
            String studentName,
            String subject,
            String ticketSubject,
            String replyBody) {

        if (studentEmail == null || studentEmail.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Student email address is required."
            );
        }

        if (subject == null || subject.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Support ticket email subject is required."
            );
        }

        if (replyBody == null || replyBody.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Support ticket reply message cannot be empty."
            );
        }

        try {
            String htmlBody = buildSupportTicketHtml(
                    studentName,
                    ticketSubject,
                    replyBody.trim()
            );

            MimeMessage message = gmailMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(getGmailFromEmail());
            helper.setTo(studentEmail.trim());
            helper.setSubject(subject.trim());
            helper.setText(htmlBody, true);

            addCcIfConfigured(helper);

            gmailMailSender.send(message);

        } catch (MessagingException ex) {
            throw new IllegalStateException(
                    "Unable to prepare the support ticket email.",
                    ex
            );
        }
    }

    // =========================================================
    // BUILD HTML SUPPORT EMAIL
    // =========================================================

    private String buildSupportTicketHtml(
            String studentName,
            String ticketSubject,
            String replyBody) {

        String safeStudentName = escapeHtml(
                studentName == null || studentName.isBlank()
                        ? "Student" : studentName.trim()
        );

        String safeSubject = escapeHtml(ticketSubject == null || ticketSubject.isBlank()
                ? "Support Ticket" : ticketSubject.trim());
        String safeBody = escapeHtml(replyBody);

        // Convert line breaks into HTML line breaks while preserving
        // the email body provided by AdminSupportController.
        safeBody = safeBody.replace("\r\n", "\n")
                .replace("\r", "\n")
                .replace("\n", "<br>");

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                    <title>Support Ticket Response</title>
                </head>
                <body style="
                    margin:0;
                    padding:0;
                    background-color:#f1f5f9;
                    font-family:Arial,Helvetica,sans-serif;
                    color:#1e293b;
                ">

                <table role="presentation"
                       width="100%%"
                       cellpadding="0"
                       cellspacing="0"
                       border="0"
                       style="background-color:#f1f5f9;padding:24px 10px;">
                    <tr>
                        <td align="center">

                            <table role="presentation"
                                   width="600"
                                   cellpadding="0"
                                   cellspacing="0"
                                   border="0"
                                   style="
                                       width:100%%;
                                       max-width:600px;
                                       background-color:#ffffff;
                                       border:1px solid #e2e8f0;
                                       border-radius:12px;
                                       overflow:hidden;
                                   ">

                                <!-- Blue Header -->
                                <tr>
                                    <td style="
                                        background-color:#1d4ed8;
                                        padding:28px 24px;
                                        text-align:center;
                                    ">
                                        <h1 style="
                                            margin:0;
                                            color:#ffffff;
                                            font-size:23px;
                                            line-height:1.4;
                                            font-weight:700;
                                        ">
                                            Sevakram Online
                                        </h1>
                                        <p style="
                                            margin:6px 0 0;
                                            color:#dbeafe;
                                            font-size:14px;
                                            line-height:1.5;
                                        ">
                                            Examination System
                                        </p>
                                    </td>
                                </tr>

                                <!-- Main Content -->
                                <tr>
                                    <td style="padding:28px 24px;">

                                        <h2 style="
                                            margin:0 0 16px;
                                            color:#0f172a;
                                            font-size:21px;
                                            line-height:1.4;
                                        ">
                                            Support Ticket Response
                                        </h2>

                                        <p style="
                                            margin:0 0 12px;
                                            font-size:15px;
                                            line-height:1.7;
                                            color:#334155;
                                        ">
                                            Dear %s,
                                        </p>

                                        <p style="
                                            margin:0 0 18px;
                                            font-size:15px;
                                            line-height:1.8;
                                            color:#334155;
                                        ">
                                            Your support ticket has received
                                            a response from our support team.
                                        </p>

                                        <!-- Ticket Details Card -->
                                        <table role="presentation"
                                               width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               border="0"
                                               style="
                                                   background-color:#eff6ff;
                                                   border:1px solid #bfdbfe;
                                                   border-radius:8px;
                                                   margin:0 0 24px;
                                               ">
                                            <tr>
                                                <td style="padding:18px 20px;">

                                                    <h3 style="
                                                        margin:0 0 14px;
                                                        color:#1d4ed8;
                                                        font-size:16px;
                                                    ">
                                                        Ticket Details
                                                    </h3>

                                                    <p style="
                                                        margin:0 0 10px;
                                                        font-size:14px;
                                                        line-height:1.6;
                                                        color:#334155;
                                                    ">
                                                        <strong>Subject:</strong>
                                                    </p>

                                                    <p style="
                                                        margin:0;
                                                        font-size:15px;
                                                        line-height:1.6;
                                                        color:#0f172a;
                                                        font-weight:600;
                                                    ">
                                                        %s
                                                    </p>

                                                </td>
                                            </tr>
                                        </table>

                                        <!-- Admin Reply -->
                                        <h3 style="
                                            margin:0 0 12px;
                                            color:#0f172a;
                                            font-size:17px;
                                        ">
                                            Our Response
                                        </h3>

                                        <table role="presentation"
                                               width="100%%"
                                               cellpadding="0"
                                               cellspacing="0"
                                               border="0"
                                               style="
                                                   background-color:#f8fafc;
                                                   border-left:4px solid #2563eb;
                                                   margin:0 0 24px;
                                               ">
                                            <tr>
                                                <td style="
                                                    padding:18px;
                                                    font-size:14px;
                                                    line-height:1.8;
                                                    color:#334155;
                                                    overflow-wrap:anywhere;
                                                ">
                                                    %s
                                                </td>
                                            </tr>
                                        </table>

                                        <p style="
                                            margin:0 0 20px;
                                            font-size:14px;
                                            line-height:1.8;
                                            color:#475569;
                                        ">
                                            If you need further assistance,
                                            please contact our support team
                                            through the Support section
                                            of the application.
                                        </p>

                                        <p style="
                                            margin:0;
                                            font-size:14px;
                                            line-height:1.8;
                                            color:#334155;
                                        ">
                                            Regards,<br>
                                            <strong>Sevakram Chavan</strong><br>
                                            Support Team<br>
                                            Sevakram Online Examination System
                                        </p>

                                    </td>
                                </tr>

                                <!-- Footer -->
                                <tr>
                                    <td style="
                                        background-color:#f8fafc;
                                        border-top:1px solid #e2e8f0;
                                        padding:18px 24px;
                                        text-align:center;
                                    ">
                                        <p style="
                                            margin:0;
                                            color:#64748b;
                                            font-size:12px;
                                            line-height:1.7;
                                        ">
                                            This is an automated notification
                                            regarding your support ticket.
                                            Please do not share your password
                                            or verification codes with anyone.
                                        </p>
                                        <p style="
                                            margin:8px 0 0;
                                            color:#94a3b8;
                                            font-size:11px;
                                        ">
                                            &copy; 2026 Sevakram Online
                                            Examination System
                                        </p>
                                    </td>
                                </tr>

                            </table>

                        </td>
                    </tr>
                </table>

                </body>
                </html>
                """.formatted(
                // Dynamic values are HTML-escaped before insertion.
                safeStudentName,
                safeSubject,
                safeBody
        );
    }

    // =========================================================
    // ESCAPE DYNAMIC HTML CONTENT
    // Prevents reply text from being interpreted as HTML.
    // =========================================================

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // =========================================================
    // SEND EMAIL THROUGH RESEND HTTP API
    // Retained for compatibility with existing callers.
    // =========================================================

    private void sendResendEmail(SimpleMailMessage message) {

        if (resendApiClient != null) {
            try {
                String[] recipients = message.getTo();

                if (recipients == null || recipients.length == 0) {
                    throw new IllegalArgumentException(
                            "Email recipient is missing."
                    );
                }

                resendApiClient.sendEmail(
                        recipients[0],
                        message.getSubject(),
                        message.getText()
                );

            } catch (Exception ex) {
                throw new IllegalStateException(
                        "Unable to send the email through the Resend service.",
                        ex
                );
            }

        } else {
            resendMailSender.send(message);
        }
    }

    // =========================================================
    // GET CONFIGURED GMAIL SENDER
    // =========================================================

    private String getGmailFromEmail() {

        if (gmailFromEmail == null || gmailFromEmail.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Email service is not configured. "
                            + "Please contact the administrator."
            );
        }

        return gmailFromEmail.trim();
    }

    // =========================================================
    // VALIDATE EMAIL AND OTP
    // =========================================================

    private void validateEmailAndOtp(String email, String otp) {

        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "A recipient email address is required."
            );
        }

        if (otp == null || otp.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "The verification code cannot be empty."
            );
        }
    }

    // =========================================================
    // ADD OPTIONAL CC FOR PLAIN-TEXT EMAILS
    // =========================================================

    private void addCcIfConfigured(SimpleMailMessage message) {

        if (ccEmail != null && !ccEmail.trim().isEmpty()) {
            message.setCc(ccEmail.trim());
        }
    }

    // =========================================================
    // ADD OPTIONAL CC FOR HTML EMAILS
    // =========================================================

    private void addCcIfConfigured(MimeMessageHelper helper)
            throws MessagingException {

        if (ccEmail != null && !ccEmail.trim().isEmpty()) {
            helper.setCc(ccEmail.trim());
        }
    }
}
