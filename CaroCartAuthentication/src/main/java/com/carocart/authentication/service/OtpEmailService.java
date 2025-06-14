package com.carocart.authentication.service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class OtpEmailService {

    @Autowired
    private JavaMailSender mailSender;

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

    /* ---------- Email helpers ---------- */
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("Verify Your Email for CaroCart User Signup");
        msg.setText(
            "Dear User,\n\n" +
            "Your One‑Time Password (OTP) for CaroCart signup is: " + otp + "\n\n" +
            "This OTP is valid for 5 minutes. Please do not share it with anyone.\n\n" +
            "Thank you,\nCaroCart Team"
        );
        mailSender.send(msg);
    }

    public void sendApprovalEmail(String toEmail) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("Your Vendor Account is Approved");
        msg.setText(
            "Dear Vendor,\n\n" +
            "Congratulations! Your vendor account has been approved by CaroCart admin.\n\n" +
            "You can now log in and start using your account.\n\n" +
            "Thank you,\nCaroCart Team"
        );
        mailSender.send(msg);
    }

    /* ---------- NEW: rejection notice ---------- */
    public void sendRejectionEmail(String toEmail) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("Vendor Registration Rejected");
        msg.setText(
            "Dear Vendor,\n\n" +
            "We regret to inform you that your vendor registration has been rejected by the CaroCart team.\n\n" +
            "If you believe this is a mistake or you need further information, please contact support at support@carocart.com.\n\n" +
            "Thank you for your interest in partnering with us.\nCaroCart Team"
        );
        mailSender.send(msg);
    }
}
