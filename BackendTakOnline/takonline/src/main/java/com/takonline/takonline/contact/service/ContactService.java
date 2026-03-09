package com.takonline.takonline.contact.service;

import com.takonline.takonline.contact.dto.ContactRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ContactService {

    private final JavaMailSender mailSender;
    private final String contactTo;
    private final String contactFrom;
    private final String mailUsername;
    private final String mailPassword;

    public ContactService(
            JavaMailSender mailSender,
            @Value("${app.contact.to}") String contactTo,
            @Value("${app.contact.from:}") String contactFrom,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword) {
        this.mailSender = mailSender;
        this.contactTo = contactTo;
        this.contactFrom = contactFrom;
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
    }

    public void sendContactEmail(ContactRequest request) {
        if (!StringUtils.hasText(mailUsername) || !StringUtils.hasText(mailPassword)) {
            String usernameState = StringUtils.hasText(mailUsername) ? "OK" : "MISSING";
            String passwordState = StringUtils.hasText(mailPassword) ? "OK" : "MISSING";
            throw new IllegalStateException(
                    "SMTP no configurado. MAIL_USERNAME=" + usernameState +
                    ", MAIL_PASS_TAK/MAIL_PASSWORD=" + passwordState + ".");
        }

        String topicLabel = "BUG".equals(request.getTopic()) ? "Bug" : "Suggestion";
        String subject = "[Tak Online] " + topicLabel + " from " + request.getReporterEmail();
        String body =
                "Topic: " + topicLabel + "\n" +
                "Reporter email: " + request.getReporterEmail() + "\n" +
                "Page: " + request.getPageUrl() + "\n\n" +
                request.getMessage();

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(contactTo);
        if (StringUtils.hasText(contactFrom)) {
            mail.setFrom(contactFrom);
        }
        mail.setReplyTo(request.getReporterEmail());
        mail.setSubject(subject);
        mail.setText(body);

        mailSender.send(mail);
    }
}
