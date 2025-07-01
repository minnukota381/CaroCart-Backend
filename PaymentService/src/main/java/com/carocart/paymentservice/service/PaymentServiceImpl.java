package com.carocart.paymentservice.service;

import com.carocart.paymentservice.dto.*;
import com.carocart.paymentservice.entity.Payment;
import com.carocart.paymentservice.feign.AddressClient;
import com.carocart.paymentservice.feign.OrderClient;
import com.carocart.paymentservice.repository.PaymentRepository;
import com.carocart.paymentservice.util.PaymentValidator;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key_id}")
    private String razorpayKey;

    @Value("${razorpay.key_secret}")
    private String razorpaySecret;

    private final AddressClient addressClient;
    private final PaymentRepository paymentRepo;
    private final PaymentValidator validator;
    private final OrderClient orderClient;

    public PaymentServiceImpl(
        PaymentRepository paymentRepo,
        PaymentValidator validator,
        OrderClient orderClient,
        AddressClient addressClient
    ) {
        this.paymentRepo = paymentRepo;
        this.validator = validator;
        this.orderClient = orderClient;
        this.addressClient = addressClient;
    }

    private List<OrderItemDTO> convertCartToOrderItems(List<CartItemDTO> cartItems) {
        return cartItems.stream().map(cart -> {
            OrderItemDTO orderItem = new OrderItemDTO();
            orderItem.setProductId(cart.getProductId());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setPrice(cart.getPrice());
            return orderItem;
        }).collect(Collectors.toList());
    }

    @Override
    public RazorpaySuccessResponse prepareRazorpayOrder(PaymentInitRequest request) {
        try {
            RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);
            int amountInPaise = (int) (request.getAmount() * 100);

            String receipt = "rcpt_" + System.currentTimeMillis();

            JSONObject payload = new JSONObject();
            payload.put("amount", amountInPaise);
            payload.put("currency", "INR");
            payload.put("receipt", receipt);
            payload.put("payment_capture", true);

            Order rpOrder = client.orders.create(payload);

            return new RazorpaySuccessResponse(
                rpOrder.get("id"),
                amountInPaise,
                "INR",
                razorpayKey
            );
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create Razorpay order: " + ex.getMessage(), ex);
        }
    }

    @Override
    public OrderResponseDTO verifyAndCreateOrder(PaymentAndOrderRequest request) {
    	
        boolean valid = validator.verifySignature(
            request.getRazorpayOrderId(),
            request.getRazorpayPaymentId(),
            request.getRazorpaySignature()
        );
        if (!valid) {
            throw new RuntimeException("Invalid Razorpay signature");
        }

        // --- Fetch formatted address using AddressClient
        String formattedAddress = null;
        try {
            AddressResponseDTO addressDto = addressClient.getAddressById(request.getAddressId());
            if (addressDto != null) {
                formattedAddress = addressDto.getFullName() + ", "
                        + addressDto.getHouseNumber() + ", "
                        + addressDto.getStreet() + ", "
                        + (addressDto.getLandmark() != null ? addressDto.getLandmark() + ", " : "")
                        + addressDto.getCity() + ", "
                        + addressDto.getState() + " - "
                        + addressDto.getPincode() + ". Phone: "
                        + addressDto.getPhoneNumber() + (addressDto.getAddressType() != null ? " [" + addressDto.getAddressType() + "]" : "");
            }
        } catch (Exception ex) {
            System.out.println("Failed to fetch or format address: " + ex.getMessage());
            formattedAddress = null; // fallback if call fails
        }

        OrderRequestDTO orderReq = new OrderRequestDTO();
        orderReq.setUserId(request.getUserId());
        orderReq.setAddressId(request.getAddressId());
        orderReq.setItems(convertCartToOrderItems(request.getCartItems()));
        orderReq.setTotalAmount(request.getAmount());
        orderReq.setShippingAddress(formattedAddress); // <-- set the address!

        // create order
        System.out.println("OrderRequestDTO userId: " + orderReq.getUserId());
        ResponseEntity<OrderResponseDTO> orderResp = orderClient.createOrder(orderReq);
        if (!orderResp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Order creation failed: " + orderResp.getBody());
        }
        OrderResponseDTO created = orderResp.getBody();

        // save payment record (same as before)
        Payment p = new Payment();
        p.setOrderId(created.getOrderId());
        p.setAmount(request.getAmount());
        p.setPaymentMethod("RAZORPAY");
        p.setPaymentId(request.getRazorpayPaymentId());
        p.setStatus("PAID");
        p.setCreatedAt(LocalDateTime.now());
        p.setUpdatedAt(LocalDateTime.now());
        paymentRepo.save(p);

        return created;
    }

    @Override
    public String refundPayment(String paymentId, double amount) {
        try {
            RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);
            JSONObject req = new JSONObject().put("amount", (int)(amount * 100));
            client.payments.refund(paymentId, req);
            return "Refund initiated";
        } catch (Exception ex) {
            throw new RuntimeException("Refund failed: " + ex.getMessage(), ex);
        }
    }

    @Override
    public PaymentResponseDTO checkStatus(String paymentId) {
        Payment p = paymentRepo.findByPaymentId(paymentId)
                      .orElseThrow(() -> new RuntimeException("Payment not found"));
        return new PaymentResponseDTO(
            p.getPaymentId(),
            p.getOrderId(),
            p.getUpiTransactionId(),
            p.getStatus(),
            p.getUpdatedAt()
        );
    }
}
