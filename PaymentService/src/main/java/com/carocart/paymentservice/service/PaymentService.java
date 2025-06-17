package com.carocart.paymentservice.service;

import com.carocart.paymentservice.dto.PaymentVerificationRequest;
import com.carocart.paymentservice.dto.PaymentResponseDTO;

public interface PaymentService {
    PaymentResponseDTO processPayment(PaymentVerificationRequest request);
}
