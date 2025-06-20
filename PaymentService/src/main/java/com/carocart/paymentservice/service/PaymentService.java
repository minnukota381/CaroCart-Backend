package com.carocart.paymentservice.service;

import com.carocart.paymentservice.dto.PaymentVerificationRequest;
import com.carocart.paymentservice.dto.PaymentResponseDTO;

public interface PaymentService {
    
    // For processing Razorpay payments (with signature verification)
    PaymentResponseDTO processPayment(PaymentVerificationRequest request);

    // For recording manual UPI payments
    PaymentResponseDTO recordManualUpiPayment(Long orderId, Long userId, Double amount, String upiTransactionId);

    // For issuing refunds
    String refundPayment(String razorpayPaymentId, double amount);
    
    PaymentResponseDTO requestUpiCollect(Long orderId, String upiId);
}
