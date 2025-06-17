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

    private final PaymentRepository paymentRepository;
    private final PaymentValidator paymentValidator;

    @Value("${razorpay.key_secret}")
    private String razorpaySecret;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentValidator paymentValidator) {
        this.paymentRepository = paymentRepository;
        this.paymentValidator = paymentValidator;
    }

    @Override
    public PaymentResponseDTO processPayment(PaymentVerificationRequest request) {
        // Step 1: Validate input
        paymentValidator.validate(request);

        // Step 2: Verify Razorpay signature
        if (!verifySignature(request)) {
            throw new RuntimeException("Invalid Razorpay signature. Payment verification failed.");
        }

        // Optional: prevent duplicate payment entries
        if (paymentRepository.existsByRazorpayPaymentId(request.getRazorpayPaymentId())) {
            throw new RuntimeException("Payment already recorded.");
        }

        // Step 3: Save payment record in DB
        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setRazorpayOrderId(request.getRazorpayOrderId());
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus("PAID");  // Changed to match typical Payment entity naming
        payment.setTransactionDate(LocalDateTime.now());  // Changed to match typical field

        Payment saved = paymentRepository.save(payment);

        // Step 4: Prepare and return response
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setPaymentId(saved.getId().toString());
        response.setStatus(saved.getStatus());
        response.setTimestamp(saved.getTransactionDate());

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
