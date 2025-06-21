package com.carocart.Notofications.service;

import com.carocart.Notofications.DTO.NotificationRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NotificationService {

    private final EmailService emailService;

    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void sendNotification(NotificationRequest request) {
        if (request == null || !StringUtils.hasText(request.getType())) {
            throw new IllegalArgumentException("Notification type is required.");
        }

        String subject;
        String body;

        switch (request.getType().toUpperCase()) {
            case "ORDER_PLACED":
                subject = "🛒 Order Placed: " + request.getOrderId();
                body = "Thank you for your order! Your order ID is " + request.getOrderId()
                        + " for product: " + request.getProductName();
                break;

            case "ORDER_DELIVERED":
                subject = "📦 Order Delivered: " + request.getOrderId();
                body = "Your order has been successfully delivered.";
                break;

            case "ORDER_CANCELLED":
                subject = "❌ Order Cancelled: " + request.getOrderId();
                body = "Your order has been cancelled.";
                break;

            case "PAYMENT_SUCCESS":
                subject = "💳 Payment Successful";
                body = "Payment of ₹" + request.getAmount() + " was successful for order " + request.getOrderId();
                break;

            case "REFUND_PROCESSED":
                subject = "💰 Refund Processed";
                body = "Your refund for order " + request.getOrderId() + " has been processed.";
                break;

            default:
                subject = "🔔 Notification";
                body = request.getMessage() != null ? request.getMessage() : "You have a new notification.";
        }

        if (StringUtils.hasText(request.getUserEmail())) {
            emailService.sendEmail(request.getUserEmail(), subject, body);
        }

        if (StringUtils.hasText(request.getVendorEmail())) {
            emailService.sendEmail(request.getVendorEmail(), subject, "[Vendor Notification] " + body);
        }

        if (StringUtils.hasText(request.getAdminEmail())) {
            emailService.sendEmail(request.getAdminEmail(), subject, "[Admin Notification] " + body);
        }
    }
}
