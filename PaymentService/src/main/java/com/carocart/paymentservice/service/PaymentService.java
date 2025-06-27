package com.carocart.paymentservice.service;

import com.carocart.paymentservice.dto.PaymentResponseDTO;
import com.carocart.paymentservice.dto.PaymentVerificationRequest;
import com.carocart.paymentservice.dto.RazorpaySuccessResponse;

public interface PaymentService {
    RazorpaySuccessResponse createRazorpayOrder(Long orderId);
    void verifyAndSavePayment(PaymentVerificationRequest request);
    PaymentResponseDTO recordManualUpiPayment(Long orderId, Long userId, Double amount, String upiTransactionId);
    String refundPayment(String razorpayPaymentId, double amount);
    PaymentResponseDTO generateUpiCollectRequest(Long orderId, String upiId);
    PaymentResponseDTO checkStatus(String paymentId);
}