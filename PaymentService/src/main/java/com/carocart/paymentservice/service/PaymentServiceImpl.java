// Updated PaymentServiceImpl with UPI Collect support
package com.carocart.paymentservice.service;

import com.carocart.paymentservice.dto.PaymentVerificationRequest;
import com.carocart.paymentservice.dto.PaymentResponseDTO;
import com.carocart.paymentservice.entity.Payment;
import com.carocart.paymentservice.repository.PaymentRepository;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.Refund;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import jakarta.annotation.PostConstruct;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentValidator paymentValidator;

    @Value("${razorpay.key_id}")
    private String razorpayKey;

    @Value("${razorpay.key_secret}")
    private String razorpaySecret;

    private RazorpayClient razorpayClient;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, PaymentValidator paymentValidator) {
        this.paymentRepository = paymentRepository;
        this.paymentValidator = paymentValidator;
    }

    @PostConstruct
    public void init() {
        try {
            this.razorpayClient = new RazorpayClient(razorpayKey, razorpaySecret);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Razorpay client", e);
        }
    }

    @Override
    public PaymentResponseDTO processPayment(PaymentVerificationRequest request) {
        paymentValidator.validate(request);

        if (!verifySignature(request)) {
            throw new RuntimeException("Invalid Razorpay signature. Payment verification failed.");
        }

        if (paymentRepository.existsByRazorpayPaymentId(request.getRazorpayPaymentId())) {
            throw new RuntimeException("Payment already recorded.");
        }

        Payment payment = new Payment();
        payment.setOrderId(request.getOrderId());
        payment.setUserId(request.getUserId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setRazorpayOrderId(request.getRazorpayOrderId());
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus("PAID");
        payment.setTransactionDate(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        return new PaymentResponseDTO(
            saved.getId().toString(),
            saved.getStatus(),
            saved.getTransactionDate()
        );
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

    @Override
    public PaymentResponseDTO recordManualUpiPayment(Long orderId, Long userId, Double amount, String upiTxnId) {
        if (upiTxnId == null || upiTxnId.trim().isEmpty()) {
            throw new IllegalArgumentException("UPI Transaction ID is required.");
        }

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId);
        payment.setAmount(amount);
        payment.setUpiTransactionId(upiTxnId);
        payment.setPaymentMethod("MANUAL_UPI");
        payment.setStatus("PENDING");
        payment.setTransactionDate(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);

        return new PaymentResponseDTO(
            saved.getId().toString(),
            saved.getStatus(),
            saved.getTransactionDate()
        );
    }

    @Override
    public String refundPayment(String razorpayPaymentId, double refundAmount) {
        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", Math.round(refundAmount * 100));

            Refund refund = razorpayClient.payments.refund(razorpayPaymentId, refundRequest);

            Optional<Payment> optionalPayment = paymentRepository.findByRazorpayPaymentId(razorpayPaymentId);
            if (optionalPayment.isPresent()) {
                Payment payment = optionalPayment.get();
                payment.setStatus("REFUNDED");
                paymentRepository.save(payment);
            }

            return "Refund initiated: " + refund.get("id");
        } catch (Exception e) {
            e.printStackTrace();
            return "Refund failed: " + e.getMessage();
        }
    }

    @Override
    public PaymentResponseDTO requestUpiCollect(Long orderId, String upiId) {
        try {
            long expiryEpoch = (System.currentTimeMillis() / 1000) + 300;
            long amountInPaise = 10000; // Replace with actual logic to fetch order amount

            JSONObject request = new JSONObject();
            request.put("amount", amountInPaise);
            request.put("currency", "INR");
            request.put("accept_partial", false);
            request.put("expire_by", expiryEpoch);
            request.put("reference_id", "UPI_COLLECT_" + orderId);
            request.put("description", "Collect request for CaroCart order");

            JSONObject customer = new JSONObject();
            customer.put("contact", "9999999999");
            customer.put("email", "user@example.com");
            request.put("customer", customer);

            request.put("notify", new JSONObject().put("email", true).put("sms", true));
            request.put("callback_method", "get");

            PaymentLink paymentLink = razorpayClient.paymentLink.create(request);

            Payment payment = new Payment();
            payment.setOrderId(orderId);
            payment.setUpiId(upiId);
            payment.setCollectRequestId(paymentLink.get("id"));
            payment.setPaymentMethod("UPI_COLLECT");
            payment.setStatus("PENDING");
            payment.setAmount(amountInPaise / 100.0);
            payment.setExpiryTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(expiryEpoch), ZoneId.systemDefault()));
            payment.setTransactionDate(LocalDateTime.now());

            Payment saved = paymentRepository.save(payment);

            PaymentResponseDTO dto = new PaymentResponseDTO();
            dto.setPaymentId(saved.getId().toString());
            dto.setStatus(saved.getStatus());
            dto.setTimestamp(saved.getTransactionDate());
            dto.setCollectRequestId(saved.getCollectRequestId());
            dto.setPaymentLink((String) paymentLink.get("short_url"));
            dto.setExpiryTime(saved.getExpiryTime());

            return dto;
        } catch (Exception e) {
            throw new RuntimeException("UPI collect request failed: " + e.getMessage(), e);
        }
    }
}