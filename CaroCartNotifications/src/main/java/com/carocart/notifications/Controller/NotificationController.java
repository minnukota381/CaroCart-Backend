// com/carocart/notifications/Controller/NotificationController.java
package com.carocart.notifications.Controller;

import com.carocart.notifications.dto.ContactMessageDTO;
import com.carocart.notifications.dto.EmailRequest;
import com.carocart.notifications.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final EmailService emailService;
    public NotificationController(EmailService emailService) { this.emailService = emailService; }

    // Accept both "to" and legacy "toEmail"
    private String resolveTo(EmailRequest r){
        if (r == null) return null;
        if (r.getTo() != null && !r.getTo().isBlank()) return r.getTo();
        if (r.getToEmail() != null && !r.getToEmail().isBlank()) return r.getToEmail();
        return null;
    }

    @PostMapping("/email")
    public void sendEmail(@RequestBody EmailRequest req) {
        String to = resolveTo(req);
        if (to == null) throw new RuntimeException("Email 'to' is required");
        if (req.getSubject() == null || req.getBody() == null) throw new RuntimeException("Subject/body required");
        try {
            if (req.isHtml()) emailService.sendHtmlMail(to, req.getSubject(), req.getBody());
            else              emailService.sendSimpleMail(req.getSubject(), req.getBody(), to);
        } catch (Exception e) {
            throw new RuntimeException("Email send failed: " + e.getMessage());
        }
    }
    // Legacy endpoint (kept for backward compatibility)
    @PostMapping("/send")
    public ResponseEntity<String> sendEmailLegacy(@RequestBody EmailRequest request) {
        String to = resolveTo(request);
        if (to == null || request.getSubject() == null || request.getSubject().isBlank()
                || request.getBody() == null || request.getBody().isBlank()) {
            return ResponseEntity.badRequest().body("Missing email fields");
        }
        try {
            if (request.isHtml()) {
                emailService.sendHtmlMail(to, request.getSubject(), request.getBody());
            } else {
                // PLAIN branch must call simple text sender (subject, body, to)
                emailService.sendSimpleMail(request.getSubject(), request.getBody(), to);
            }
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to send email: " + e.getMessage());
        }
    }


    @PostMapping("/contact")
    public ResponseEntity<String> receiveContactMessage(@RequestBody ContactMessageDTO messageDTO) {
        if (messageDTO == null ||
            messageDTO.getName() == null || messageDTO.getName().isBlank() ||
            messageDTO.getEmail() == null || messageDTO.getEmail().isBlank() ||
            messageDTO.getTopic() == null || messageDTO.getTopic().isBlank() ||
            messageDTO.getMessage() == null || messageDTO.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body("Missing required fields");
        }
        String to = "CaroCartTeam@gmail.com";
        String subject = "[CaroCart Contact] " + messageDTO.getTopic();
        String body = "You have received a new Contact Us message:\n\n"
                + "Name: " + messageDTO.getName() + "\n"
                + "Email: " + messageDTO.getEmail() + "\n"
                + "Topic: " + messageDTO.getTopic() + "\n"
                + "Message:\n" + messageDTO.getMessage();
        try {
            emailService.sendSimpleMail(subject, body, to);
            return ResponseEntity.ok("Message sent to CaroCart team");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to send: " + e.getMessage());
        }
    }
}
