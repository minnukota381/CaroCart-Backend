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
    @PostMapping("/contact")
    public ResponseEntity<String> receiveContactMessage(@RequestBody ContactMessageDTO messageDTO) {
        if (messageDTO == null ||
            messageDTO.getName() == null || messageDTO.getName().isBlank() ||
            messageDTO.getEmail() == null || messageDTO.getEmail().isBlank() ||
            messageDTO.getTopic() == null || messageDTO.getTopic().isBlank() ||
            messageDTO.getMessage() == null || messageDTO.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body("Missing required fields");
        }

        // Compose the email
        String to = "CaroCartTeam@gmail.com"; // your support email
        String subject = "[CaroCart Contact] " + messageDTO.getTopic();
        String body = "You have received a new Contact Us message:\n\n"
                + "Name: " + messageDTO.getName() + "\n"
                + "Email: " + messageDTO.getEmail() + "\n"
                + "Topic: " + messageDTO.getTopic() + "\n"
                + "Message:\n" + messageDTO.getMessage();

        EmailRequest request = new EmailRequest();
        request.setToEmail(to);
        request.setSubject(subject);
        request.setBody(body);

        try {
            emailService.sendEmail(request);
            return ResponseEntity.ok("Message sent to CaroCart team");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to send: " + e.getMessage());
        }
    }

}
