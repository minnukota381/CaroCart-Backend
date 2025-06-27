package com.carocart.paymentservice.service;

import com.carocart.paymentservice.dto.*;
import com.carocart.paymentservice.entity.Payment;
import com.carocart.paymentservice.feign.OrderFeignClient;
import com.carocart.paymentservice.repository.PaymentRepository;
import com.carocart.paymentservice.util.PaymentValidator;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    private final PaymentRepository paymentRepo;
    private final PaymentValidator validator;
    private final OrderFeignClient orderClient;

    public PaymentServiceImpl(PaymentRepository paymentRepo, PaymentValidator validator, OrderFeignClient orderClient) {
        this.paymentRepo = paymentRepo;
        this.validator = validator;
        this.orderClient = orderClient;
    }

    @Override
    public RazorpaySuccessResponse createRazorpayOrder(Long orderId) {
        // Validate orderId and fetch totalAmount
        ResponseEntity<Map<String, Object>> orderResponse = orderClient.getOrderById(orderId);
        if (!orderResponse.getStatusCode().is2xxSuccessful()) throw new RuntimeException("Order not found");

        Double amount = Double.parseDouble(orderResponse.getBody().get("totalAmount").toString());
        int amountInPaise = (int) (amount * 100);

        try {
            RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "order_rcptid_" + orderId);
            orderRequest.put("payment_capture", true);
            Order razorpayOrder = client.orders.create(orderRequest);

            Payment payment = new Payment();
            payment.setOrderId(orderId);
            payment.setAmount(amount);
            payment.setStatus("CREATED");
            payment.setPaymentId(razorpayOrder.get("id"));
            paymentRepo.save(payment);

            return new RazorpaySuccessResponse(
                razorpayOrder.get("id"), amountInPaise, "INR", razorpayKey);
        } catch (Exception e) {
            throw new RuntimeException("Razorpay order creation failed: " + e.getMessage());
        }
    }

    @Override
    public void verifyAndSavePayment(PaymentVerificationRequest request) {
        boolean isValid = validator.verifySignature(
            request.getRazorpayOrderId(),
            request.getRazorpayPaymentId(),
            request.getRazorpaySignature(),
            razorpaySecret);

        if (!isValid) throw new RuntimeException("Invalid Razorpay signature");

        Payment payment = paymentRepo.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Payment record not found"));

        payment.setStatus("SUCCESS");
        payment.setPaymentId(request.getRazorpayPaymentId());
        paymentRepo.save(payment);

        orderClient.updatePaymentStatus(request.getOrderId(),
            new PaymentUpdateRequestDTO(request.getRazorpayPaymentId(), "PAID"));
    }

    @Override
    public PaymentResponseDTO recordManualUpiPayment(Long orderId, Long userId, Double amount, String upiTxnId) {
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setPaymentId("manual-upi-" + UUID.randomUUID());
        payment.setPaymentMethod("MANUAL_UPI");
        payment.setStatus("PENDING");
        paymentRepo.save(payment);

        return new PaymentResponseDTO(payment.getPaymentId(), orderId, upiTxnId, payment.getStatus(), payment.getUpdatedAt());
    }

    @Override
    public String refundPayment(String paymentId, double amount) {
        try {
            RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", (int)(amount * 100));
            client.payments.refund(paymentId, refundRequest);
            return "Refund initiated";
        } catch (Exception e) {
            throw new RuntimeException("Refund failed: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponseDTO generateUpiCollectRequest(Long orderId, String upiId) {
        // Simulated UPI collect
        return new PaymentResponseDTO("COLLECT-" + UUID.randomUUID(), orderId, upiId, "PENDING", null);
    }

    @Override
    public PaymentResponseDTO checkStatus(String paymentId) {
        Payment payment = paymentRepo.findByPaymentId(paymentId).orElseThrow(() ->
                new RuntimeException("Payment not found"));
        return new PaymentResponseDTO(payment.getPaymentId(), payment.getOrderId(),
                payment.getUpiTransactionId(), payment.getStatus(), payment.getUpdatedAt());
    }
}