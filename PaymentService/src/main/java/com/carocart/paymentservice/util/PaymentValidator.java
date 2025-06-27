package com.carocart.paymentservice.util;

import com.razorpay.Utils;
import org.springframework.stereotype.Component;

@Component
public class PaymentValidator {
    public boolean isValid(String orderId, String paymentId, String signature) {
        try {
            return Utils.verifySignature(signature, orderId + "|" + paymentId, System.getenv("RAZORPAY_SECRET"));
        } catch (Exception e) {
            return false;
        }
    }
}
