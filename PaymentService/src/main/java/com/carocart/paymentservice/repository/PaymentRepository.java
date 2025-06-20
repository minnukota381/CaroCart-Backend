package com.carocart.paymentservice.repository;

import com.carocart.paymentservice.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByRazorpayPaymentId(String razorpayPaymentId);
}
