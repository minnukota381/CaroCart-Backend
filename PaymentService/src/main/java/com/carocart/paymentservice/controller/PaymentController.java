package com.carocart.paymentservice.controller;

import com.carocart.paymentservice.dto.PaymentUpdateRequestDTO;
import com.carocart.paymentservice.dto.PaymentVerificationRequest;
import com.carocart.paymentservice.entity.Payment;
import com.carocart.paymentservice.feign.OrderFeignClient;
import com.carocart.paymentservice.repository.PaymentRepository;
import com.carocart.paymentservice.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Value("${razorpay.key_id}")
    private String razorpayKey;

    @Value("${razorpay.key_secret}")
    private String razorpaySecret;

    private RazorpayClient razorpayClient;
    private final OrderFeignClient orderFeignClient;

	private PaymentRepository paymentRepository;

	// Add this field to your controller
	private final PaymentService paymentService;

	public PaymentController(OrderFeignClient orderFeignClient, PaymentRepository paymentRepository, PaymentService paymentService) {
	    this.orderFeignClient = orderFeignClient;
	    this.paymentRepository = paymentRepository;
	    this.paymentService = paymentService;
	}



    @PostConstruct
    public void init() throws Exception {
        this.razorpayClient = new RazorpayClient(razorpayKey, razorpaySecret);
    }

    @GetMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestParam Long orderId) {
        try {
            ResponseEntity<Map<String, Object>> response = orderFeignClient.getOrderById(orderId);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return ResponseEntity.badRequest().body("Invalid orderId or order not found.");
            }

            Map<String, Object> order = response.getBody();
            Double amount = Double.parseDouble(order.get("totalAmount").toString());
            Long amountInPaise = Math.round(amount * 100);

            JSONObject options = new JSONObject();
            options.put("amount", amountInPaise);
            options.put("currency", "INR");
            options.put("receipt", "order_rcptid_" + orderId);
            options.put("payment_capture", 1);

            Order razorpayOrder = razorpayClient.orders.create(options);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("id", razorpayOrder.get("id"));
            responseBody.put("currency", razorpayOrder.get("currency"));
            responseBody.put("amount", razorpayOrder.get("amount"));
            responseBody.put("key", razorpayKey);
            responseBody.put("orderId", orderId);

            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create Razorpay order: " + e.getMessage());
        }
    }
    @PostMapping("/verify-and-update")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        try {
            String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
            String expectedSignature = hmacSha256(payload, razorpaySecret);

            if (!expectedSignature.equals(request.getRazorpaySignature())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Razorpay signature.");
            }

            // ✅ 1. Save to payment table
            Payment payment = new Payment();
            payment.setOrderId(request.getOrderId());
            payment.setUserId(request.getUserId());
            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setRazorpayOrderId(request.getRazorpayOrderId());
            payment.setRazorpaySignature(request.getRazorpaySignature());
            payment.setAmount(request.getAmount() / 100.0); // Convert paise to rupees
            payment.setStatus("PAID");
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setTransactionDate(LocalDateTime.now());

            paymentRepository.save(payment); // ✅ Save payment record

            // ✅ 2. Update order status via Feign
            PaymentUpdateRequestDTO updateRequest = new PaymentUpdateRequestDTO();
            updateRequest.setStatus("PAID");
            updateRequest.setPaymentId(request.getRazorpayPaymentId());

            orderFeignClient.updatePaymentStatus(request.getOrderId(), updateRequest);

            return ResponseEntity.ok("Payment verified and saved.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body("Payment verification failed: " + e.getMessage());
        }
    }

    private String hmacSha256(String data, String secret) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secretKey);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes());

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
