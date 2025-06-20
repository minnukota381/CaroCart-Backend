package com.carocart.paymentservice.repository;

import com.carocart.paymentservice.entity.Payment;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    boolean existsByRazorpayPaymentId(String razorpayPaymentId);
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);
    
    Optional<Payment> findByCollectRequestIdAndStatus(String collectRequestId, String status);


}
