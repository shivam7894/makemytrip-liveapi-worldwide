package com.mmt.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_payment_order", columnList = "orderId"),
        @Index(name = "idx_payment_booking", columnList = "bookingId")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long bookingId;
    private Long userId;
    private String provider;
    private String orderId;
    private String paymentId;
    private String status;
    private Double amount;
    private String currency;
    private String signature;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String refundId;
    private Double refundAmount;
}
