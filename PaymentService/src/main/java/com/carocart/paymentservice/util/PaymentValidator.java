package com.carocart.paymentservice.util;

import com.razorpay.Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentValidator {
    @Value("${razorpay.key_secret}")
    private String razorpaySecret;

    /**
     * Verifies that the signature returned by Razorpay is valid
     * for the given orderId and paymentId.
     */
    public boolean verifySignature(String razorpayOrderId,
                                   String razorpayPaymentId,
                                   String razorpaySignature) {
        try {
            // Razorpay expects payload = orderId + "|" + paymentId
            String payload = razorpayOrderId + "|" + razorpayPaymentId;
            // This will throw if invalid
            Utils.verifySignature(payload, razorpaySignature, razorpaySecret);
            return true;
        } catch (Exception e) {
            // log e if you like
            return false;
        }
    }
}
