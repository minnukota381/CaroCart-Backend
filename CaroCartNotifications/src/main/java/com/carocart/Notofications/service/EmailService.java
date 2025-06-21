package com.carocart.Notofications.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${from.email}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromEmail);
            mail.setTo(to);
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);

            System.out.println("✅ Email sent to: " + to);
        } catch (Exception e) {
            System.out.println("❌ Error sending email to: " + to);
            e.printStackTrace();
        }
    }

    public void sendEmailToMultiple(String[] recipients, String subject, String body) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromEmail);
            mail.setTo(recipients);
            mail.setSubject(subject);
            mail.setText(body);
            mailSender.send(mail);

            System.out.println("✅ Email sent to multiple: " + String.join(", ", recipients));
        } catch (Exception e) {
            System.out.println("❌ Error sending email to multiple recipients.");
            e.printStackTrace();
        }
    }
}
