package com.carocart.authentication.service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.carocart.authentication.dto.EmailRequest;
import com.carocart.authentication.feign.NotificationClient;

@Service
public class OtpEmailService {

    // Use Notifications microservice (via Feign) instead of JavaMailSender
    @Autowired
    private NotificationClient notificationClient;

    // Store OTPs temporarily: email -> OTP
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    /* ---------- OTP helpers ---------- */
    public String generateOtp() {
        Random random = new Random();
        int otp = 100_000 + random.nextInt(900_000);
        return String.valueOf(otp);
    }

    public void storeOtp(String email, String otp) {
        otpStorage.put(email, otp);
    }

    public boolean validateOtp(String email, String otp) {
        String storedOtp = otpStorage.get(email);
        return storedOtp != null && storedOtp.equals(otp);
    }

    public void removeOtp(String email) {
        otpStorage.remove(email);
    }

    /* ---------- Email helpers (via Notifications MS) ---------- */
    public void sendOtpEmail(String toEmail, String otp) {
        EmailRequest req = new EmailRequest();
        req.setToEmail(toEmail);
        req.setSubject("Verify Your Email • CaroCart OTP");
        req.setBody(
            "Dear User,\n\n" +
            "Your One-Time Password (OTP) for CaroCart is: " + otp + "\n\n" +
            "This OTP is valid for 5 minutes. Please do not share it with anyone.\n\n" +
            "Thank you,\nCaroCart Team"
        );
        notificationClient.sendEmail(req);
    }

    public void sendApprovalEmail(String toEmail) {
        EmailRequest req = new EmailRequest();
        req.setToEmail(toEmail);
        req.setSubject("Your CaroCart Vendor Account is Approved");
        req.setBody(
            "Dear Vendor,\n\n" +
            "Congratulations! Your vendor account has been approved by the CaroCart team.\n\n" +
            "You can now log in and start using your account.\n\n" +
            "Thank you,\nCaroCart Team"
        );
        notificationClient.sendEmail(req);
    }

    public void sendRejectionEmail(String toEmail) {
        EmailRequest req = new EmailRequest();
        req.setToEmail(toEmail);
        req.setSubject("CaroCart Vendor Registration Rejected");
        req.setBody(
            "Dear Vendor,\n\n" +
            "We regret to inform you that your vendor registration has been rejected by the CaroCart team.\n\n" +
            "If you believe this is a mistake or need further information, please contact support at support@carocart.com.\n\n" +
            "Thank you for your interest in partnering with us.\n" +
            "CaroCart Team"
        );
        notificationClient.sendEmail(req);
    }
}
