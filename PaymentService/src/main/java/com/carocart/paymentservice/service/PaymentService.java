
package com.carocart.paymentservice.service;

import com.carocart.paymentservice.dto.*;

public interface PaymentService {
    RazorpaySuccessResponse prepareRazorpayOrder(PaymentInitRequest request);
    OrderResponseDTO verifyAndCreateOrder(PaymentAndOrderRequest request);
   
    String refundPayment(String paymentId, double amount);
    PaymentResponseDTO checkStatus(String paymentId);
}
