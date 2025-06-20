package com.carocart.paymentservice.service;

import com.carocart.paymentservice.dto.PaymentVerificationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class PaymentValidator {

    public void validate(PaymentVerificationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (request.getOrderId() == null || request.getOrderId() <= 0) {
            throw new IllegalArgumentException("Invalid order ID");
        }

        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        if (!StringUtils.hasText(request.getPaymentMethod())) {
            throw new IllegalArgumentException("Payment method is required");
        }

        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new IllegalArgumentException("User ID is required and must be valid");
        }

        if (!StringUtils.hasText(request.getRazorpayPaymentId()) ||
            !StringUtils.hasText(request.getRazorpayOrderId()) ||
            !StringUtils.hasText(request.getRazorpaySignature())) {
            throw new IllegalArgumentException("Razorpay details are required for verification");
        }
    }
}
