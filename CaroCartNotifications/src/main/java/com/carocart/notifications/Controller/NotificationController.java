package com.carocart.notifications.Controller;

import com.carocart.notifications.dto.EmailRequest;
import com.carocart.notifications.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest request) {
        if (request == null || 
            request.getToEmail() == null || request.getToEmail().isBlank() ||
            request.getSubject() == null || request.getSubject().isBlank() ||
            request.getBody() == null || request.getBody().isBlank()) {
            return ResponseEntity.badRequest().body("Missing email fields");
        }

        try {
            emailService.sendEmail(request);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to send email: " + e.getMessage());
        }
    }
}
