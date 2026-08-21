package com.mmt.repository;

import com.mmt.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByOrderId(String orderId);
    Optional<PaymentTransaction> findTopByBookingIdOrderByCreatedAtDesc(Long bookingId);
    Optional<PaymentTransaction> findByPaymentId(String paymentId);
}
