package com.carocart.paymentservice.service;

import com.carocart.paymentservice.dto.PaymentVerificationRequest;
import com.carocart.paymentservice.dto.PaymentResponseDTO;
import com.carocart.paymentservice.entity.Payment;
import com.carocart.paymentservice.repository.PaymentRepository;
import com.razorpay.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentValidator paymentValidator;

    @Value("${razorpay.key_secret}")
    private String razorpaySecret;

    @Override
    public PaymentResponseDTO processPayment(PaymentVerificationRequest request) {
        // Step 1: Validate input
        paymentValidator.validate(request);

        // Step 2: Verify Razorpay signature
        if (!verifySignature(request)) {
            throw new RuntimeException("Invalid payment signature. Possible tampering detected.");
        }

        // Step 3: Save payment details to DB
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentStatus("PAID");
        payment.setPaymentDate(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        // Step 4: Build response DTO
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setPaymentId(String.valueOf(saved.getId()));
        response.setStatus(saved.getPaymentStatus());
        response.setTimestamp(saved.getPaymentDate());

        return response;
    }

    private boolean verifySignature(PaymentVerificationRequest request) {
        try {
            String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
            return Utils.verifySignature(payload, request.getRazorpaySignature(), razorpaySecret);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
