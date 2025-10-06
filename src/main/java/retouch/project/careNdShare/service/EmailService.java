package retouch.project.careNdShare.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public void sendPasswordResetEmail(String toEmail, String resetToken, String firstName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String resetLink = baseUrl + "/reset-password?token=" + resetToken;

            String subject = "Care & Share - Password Reset Request";
            String htmlContent = buildPasswordResetEmail(firstName, resetLink);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    private String buildPasswordResetEmail(String firstName, String resetLink) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("    <meta charset=\"UTF-8\">");
        html.append("    <style>");
        html.append("        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append("        .container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append("        .header { background: linear-gradient(135deg, #28a745, #218838); padding: 20px; text-align: center; color: white; border-radius: 10px 10px 0 0; }");
        html.append("        .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }");
        html.append("        .button { display: inline-block; padding: 12px 30px; background: #28a745; color: white; text-decoration: none; border-radius: 5px; font-weight: bold; }");
        html.append("        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }");
        html.append("    </style>");
        html.append("</head>");
        html.append("<body>");
        html.append("    <div class=\"container\">");
        html.append("        <div class=\"header\">");
        html.append("            <h1>Care & Share</h1>");
        html.append("            <p>Password Reset Request</p>");
        html.append("        </div>");
        html.append("        <div class=\"content\">");
        html.append("            <h2>Hello, ").append(firstName).append("!</h2>");
        html.append("            <p>You requested to reset your password for your Care & Share account.</p>");
        html.append("            <p>Click the button below to reset your password. This link will expire in 30 minutes.</p>");
        html.append("            <p style=\"text-align: center;\">");
        html.append("                <a href=\"").append(resetLink).append("\" class=\"button\">Reset Password</a>");
        html.append("            </p>");
        html.append("            <p>If the button doesn't work, copy and paste this link in your browser:</p>");
        html.append("            <p style=\"word-break: break-all; background: #e9ecef; padding: 10px; border-radius: 5px;\">");
        html.append("                ").append(resetLink);
        html.append("            </p>");
        html.append("            <p>If you didn't request this password reset, please ignore this email.</p>");
        html.append("        </div>");
        html.append("        <div class=\"footer\">");
        html.append("            <p>&copy; 2025 Care & Share. All rights reserved.</p>");
        html.append("        </div>");
        html.append("    </div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }
}