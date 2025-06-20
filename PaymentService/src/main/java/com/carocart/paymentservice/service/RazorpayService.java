package com.carocart.paymentservice.service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RazorpayService {

    private final RazorpayClient razorpayClient;

    public RazorpayService(@Value("${razorpay.key_id}") String key,
                           @Value("${razorpay.key_secret}") String secret) {
        try {
            this.razorpayClient = new RazorpayClient(key, secret);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Razorpay client", e);
        }
    }

    public Map<String, Object> createRazorpayOrder(Long orderId, Double totalAmount) throws Exception {
        long amountInPaise = Math.round(totalAmount * 100);

        JSONObject options = new JSONObject();
        options.put("amount", amountInPaise);
        options.put("currency", "INR");
        options.put("receipt", "order_rcptid_" + orderId);
        options.put("payment_capture", 1);

        Order razorpayOrder = razorpayClient.orders.create(options);

        Map<String, Object> response = new HashMap<>();
        response.put("id", razorpayOrder.get("id"));
        response.put("currency", razorpayOrder.get("currency"));
        response.put("amount", razorpayOrder.get("amount"));
        return response;
    }

    public Payment fetchPaymentById(String paymentId) throws Exception {
        return razorpayClient.payments.fetch(paymentId);
    }

    public String initiateRefund(String paymentId, String reason) throws Exception {
        Payment payment = fetchPaymentById(paymentId);

        if (!"captured".equals(payment.get("status"))) {
            throw new IllegalArgumentException("Payment not captured, refund not possible.");
        }

        JSONObject refundRequest = new JSONObject();
        refundRequest.put("payment_id", paymentId);

        JSONObject notes = new JSONObject();
        notes.put("reason", reason);
        refundRequest.put("notes", notes);

        razorpayClient.payments.refund(refundRequest);

        return "Refund initiated successfully for payment ID: " + paymentId;
    }
}
