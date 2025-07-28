package com.carocart.delivery.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeliveryBoyOtpEmailService {

    @Autowired
    private JavaMailSender mailSender;

    // Store OTPs temporarily: email -> OTP
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    // Generate a random 6-digit OTP
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

    // Send OTP Email (customize subject/body if you wish)
    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setSubject("CaroCart Delivery Boy - OTP for Password Reset");
        msg.setText(
            "Dear Delivery Partner,\n\n" +
            "Your One-Time Password (OTP) for CaroCart delivery login/password reset is: " + otp + "\n\n" +
            "This OTP is valid for 5 minutes. Please do not share it with anyone.\n\n" +
            "Thank you,\nCaroCart Team"
        );
        mailSender.send(msg);
    }

    // Optional: Add other delivery-specific mails if you want (approval, etc)
}
