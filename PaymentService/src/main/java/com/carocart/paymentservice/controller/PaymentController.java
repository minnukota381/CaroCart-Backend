package com.carocart.paymentservice.controller;

import com.carocart.paymentservice.dto.ManualUpiPaymentRequest;
import com.carocart.paymentservice.dto.PaymentResponseDTO;
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
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public PaymentController(OrderFeignClient orderFeignClient,
                             PaymentRepository paymentRepository,
                             PaymentService paymentService) {
        this.orderFeignClient = orderFeignClient;
        this.paymentRepository = paymentRepository;
        this.paymentService = paymentService;
    }

    @PostConstruct
    public void init() throws Exception {
        this.razorpayClient = new RazorpayClient(razorpayKey, razorpaySecret);
    }

    // ✅ Create Razorpay Order
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    // ✅ Razorpay Payment Verification & Save
    @PostMapping("/verify-and-update")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        try {
            String payload = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
            String expectedSignature = hmacSha256(payload, razorpaySecret);

            if (!expectedSignature.equals(request.getRazorpaySignature())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Razorpay signature.");
            }

            // Save payment
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

            paymentRepository.save(payment);

            // Update order status
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

    // ✅ Manual UPI Submission via DTO
    @PostMapping("/upi/manual")
    public ResponseEntity<?> recordManualUpiPayment(@RequestBody ManualUpiPaymentRequest request) {
        try {
            PaymentResponseDTO response = paymentService.recordManualUpiPayment(
                request.getOrderId(),
                request.getUserId(),
                request.getAmount(),
                request.getUpiTransactionId()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Manual UPI failed: " + e.getMessage());
        }
    }

    // ✅ Refund Endpoint
    @PostMapping("/refund")
    public ResponseEntity<?> refundPayment(@RequestParam String razorpayPaymentId, @RequestParam double amount) {
        try {
            String result = paymentService.refundPayment(razorpayPaymentId, amount);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Refund failed: " + e.getMessage());
        }
    }
    @PostMapping("/upi/verify")
    public ResponseEntity<?> verifyManualUpi(@RequestParam Long paymentId) {
        try {
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            if (paymentOpt.isEmpty()) return ResponseEntity.notFound().build();

            Payment payment = paymentOpt.get();
            payment.setStatus("PAID");
            paymentRepository.save(payment);

            // Update order status as PAID
            PaymentUpdateRequestDTO updateRequest = new PaymentUpdateRequestDTO();
            updateRequest.setStatus("PAID");
            updateRequest.setPaymentId("MANUAL_UPI_" + payment.getId());

            orderFeignClient.updatePaymentStatus(payment.getOrderId(), updateRequest);

            return ResponseEntity.ok("Payment marked as verified.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Verification failed: " + e.getMessage());
        }
    }
    
    //Upi transactoions
    @PostMapping("/upi/collect")
    public ResponseEntity<?> requestUpiCollect(@RequestParam Long orderId, @RequestParam String upiId) {
        try {
            ResponseEntity<Map<String, Object>> response = orderFeignClient.getOrderById(orderId);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return ResponseEntity.badRequest().body("Invalid orderId or order not found.");
            }

            Map<String, Object> order = response.getBody();
            Double amount = Double.parseDouble(order.get("totalAmount").toString());
            Long amountInPaise = Math.round(amount * 100);

            JSONObject request = new JSONObject();
            request.put("amount", amountInPaise);
            request.put("currency", "INR");
            request.put("accept_partial", false);
            request.put("expire_by", (System.currentTimeMillis() / 1000) + 300); // 5 min expiry
            request.put("reference_id", "UPI_COLLECT_" + orderId);
            request.put("description", "Collect request for CaroCart order");

            JSONObject customer = new JSONObject();
            customer.put("contact", "9999999999"); // Optional
            customer.put("email", "user@example.com"); // Optional
            request.put("customer", customer);

            request.put("notify", new JSONObject().put("email", true).put("sms", true));
            request.put("callback_url", "https://localhost:8086/api/payments/upi/verify");
            request.put("callback_method", "get");

            com.razorpay.PaymentLink paymentLink = razorpayClient.paymentLink.create(request);

            // Optionally save this link to DB for tracking
            return ResponseEntity.ok(Map.of(
                    "paymentLink", paymentLink.get("short_url"),
                    "requestId", paymentLink.get("id"),
                    "expiry", 5 * 60 // 5 minutes
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("UPI Collect request failed: " + e.getMessage());
        }
    }

 // ✅ Check payment status (used for polling UPI collect)
    @GetMapping("/status/{paymentId}")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable Long paymentId) {
        Optional<Payment> payment = paymentRepository.findById(paymentId);
        if (payment.isEmpty()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(Map.of("status", payment.get().getStatus()));
    }


    // ✅ Signature Generator (for Razorpay Verification)
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
