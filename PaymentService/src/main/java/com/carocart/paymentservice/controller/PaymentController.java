package com.carocart.paymentservice.controller;

import com.carocart.paymentservice.dto.*;
import com.carocart.paymentservice.service.PaymentService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // 1. Prepare Razorpay Order (Order after payment)
    @PostMapping("/prepare-razorpay-order")
    public ResponseEntity<?> prepareRazorpayOrder(@RequestBody PaymentInitRequest request) {
        try {
            RazorpaySuccessResponse response = paymentService.prepareRazorpayOrder(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    // 2. Verify payment and create order after payment
    @PostMapping("/verify-and-create-order")
    public ResponseEntity<?> verifyAndCreateOrder(@RequestBody PaymentAndOrderRequest request) {
    	try {
            System.out.println("Received PaymentAndOrderRequest: " + new ObjectMapper().writeValueAsString(request));
        } catch (Exception e) {
            System.out.println("Error serializing request: " + e.getMessage());
        }
         System.out.println("userId: " + request.getUserId());
        try {
            OrderResponseDTO orderResponse = paymentService.verifyAndCreateOrder(request);
            return ResponseEntity.ok(orderResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Order/payment failed: " + e.getMessage());
        }
    }
    // 6. Refund a payment (Razorpay)
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

    // 7. Payment status polling
    @GetMapping("/status/{paymentId}")
    public ResponseEntity<?> checkPaymentStatus(@PathVariable String paymentId) {
        try {
            PaymentResponseDTO response = paymentService.checkStatus(paymentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment not found: " + e.getMessage());
        }
    }
}