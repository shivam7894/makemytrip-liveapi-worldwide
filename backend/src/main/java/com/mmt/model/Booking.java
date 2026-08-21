package com.mmt.model;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="bookings") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id; Long userId; String bookingType; Long itemId; Long roomId; String passengerName; String passengerEmail; String seatNumber; String roomType; Double totalAmount; String status; String cancellationReason; Double refundAmount; String refundStatus; LocalDateTime expectedRefundDate; LocalDateTime bookingTime; LocalDateTime cancelledAt; String paymentProvider; String paymentOrderId; String paymentId; String paymentStatus; }
