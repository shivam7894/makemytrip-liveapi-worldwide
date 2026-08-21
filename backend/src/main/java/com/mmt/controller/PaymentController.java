package com.mmt.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.mmt.model.Booking;
import com.mmt.model.PaymentTransaction;
import com.mmt.repository.BookingRepository;
import com.mmt.repository.PaymentTransactionRepository;
import com.mmt.service.RazorpayPaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {
    private final BookingRepository bookings;
    private final PaymentTransactionRepository payments;
    private final RazorpayPaymentService razorpay;
    @Value("${payment.upi-vpa:}") private String upiVpa;
    @Value("${payment.merchant-name:MakeMyTrip Clone}") private String merchantName;

    public PaymentController(BookingRepository bookings, PaymentTransactionRepository payments, RazorpayPaymentService razorpay) {
        this.bookings = bookings; this.payments = payments; this.razorpay = razorpay;
    }

    @GetMapping("/providers")
    public Map<String,Object> providers() {
        Map<String,Object> out = new LinkedHashMap<>();
        out.put("razorpay", razorpay.configured());
        out.put("upiQr", upiVpa != null && !upiVpa.isBlank());
        out.put("razorpayKeyId", razorpay.configured() ? razorpay.keyId() : "");
        out.put("merchantName", merchantName);
        return out;
    }

    @PostMapping("/razorpay/order")
    public Map<String,Object> createOrder(@RequestBody Map<String,Object> body) throws Exception {
        Long bookingId = Long.valueOf(body.get("bookingId").toString());
        Booking b = bookings.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!"PENDING_PAYMENT".equalsIgnoreCase(b.getStatus()) && !"CONFIRMED".equalsIgnoreCase(b.getStatus())) throw new RuntimeException("Booking is not payable");
        double amount = b.getTotalAmount() == null ? 0 : b.getTotalAmount();
        JsonNode order = razorpay.createOrder(amount, "MMT-" + bookingId, String.valueOf(bookingId));
        String orderId = order.path("id").asText();
        PaymentTransaction tx = PaymentTransaction.builder().bookingId(bookingId).userId(b.getUserId()).provider("RAZORPAY").orderId(orderId).status("CREATED").amount(amount).currency("INR").createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        payments.save(tx);
        b.setPaymentProvider("RAZORPAY"); b.setPaymentOrderId(orderId); b.setPaymentStatus("CREATED"); bookings.save(b);
        return Map.of("keyId", razorpay.keyId(), "orderId", orderId, "amount", Math.round(amount * 100), "currency", "INR", "bookingId", bookingId, "merchantName", merchantName);
    }

    @PostMapping("/razorpay/verify")
    public Map<String,Object> verify(@RequestBody Map<String,String> body) {
        String orderId = body.get("razorpay_order_id"), paymentId = body.get("razorpay_payment_id"), signature = body.get("razorpay_signature");
        PaymentTransaction tx = payments.findByOrderId(orderId).orElseThrow(() -> new RuntimeException("Payment order not found"));
        if (!razorpay.verifySignature(orderId, paymentId, signature)) throw new RuntimeException("Invalid Razorpay signature");
        tx.setPaymentId(paymentId); tx.setSignature(signature); tx.setStatus("PAID"); tx.setUpdatedAt(LocalDateTime.now()); payments.save(tx);
        Booking b = bookings.findById(tx.getBookingId()).orElseThrow(() -> new RuntimeException("Booking not found"));
        b.setStatus("CONFIRMED"); b.setPaymentId(paymentId); b.setPaymentStatus("PAID"); b.setPaymentProvider("RAZORPAY"); b.setPaymentOrderId(orderId); bookings.save(b);
        return Map.of("success", true, "bookingId", b.getId(), "paymentId", paymentId);
    }

    @PostMapping("/razorpay/refund")
    public Map<String,Object> refund(@RequestBody Map<String,Object> body) throws Exception {
        Long bookingId = Long.valueOf(body.get("bookingId").toString());
        double amount = Double.parseDouble(body.get("amount").toString());
        PaymentTransaction tx = payments.findTopByBookingIdOrderByCreatedAtDesc(bookingId).orElseThrow(() -> new RuntimeException("Payment transaction not found"));
        if (!"PAID".equalsIgnoreCase(tx.getStatus()) || tx.getPaymentId() == null) throw new RuntimeException("No captured Razorpay payment is available for refund");
        JsonNode refund = razorpay.refund(tx.getPaymentId(), amount, "MMT-REFUND-" + bookingId);
        tx.setRefundId(refund.path("id").asText()); tx.setRefundAmount(amount); tx.setStatus("REFUND_INITIATED"); tx.setUpdatedAt(LocalDateTime.now()); payments.save(tx);
        return Map.of("refundId", tx.getRefundId(), "status", "REFUND_INITIATED", "amount", amount);
    }

    @GetMapping("/booking/{bookingId}")
    public Map<String,Object> bookingPayment(@PathVariable Long bookingId) {
        PaymentTransaction tx = payments.findTopByBookingIdOrderByCreatedAtDesc(bookingId).orElse(null);
        if (tx == null) return Map.of("status", "NOT_CREATED");
        Map<String,Object> out = new LinkedHashMap<>(); out.put("id", tx.getId()); out.put("provider", tx.getProvider()); out.put("status", tx.getStatus()); out.put("orderId", tx.getOrderId()); out.put("paymentId", tx.getPaymentId()); out.put("amount", tx.getAmount()); out.put("refundId", tx.getRefundId()); return out;
    }

    @GetMapping("/upi-qr")
    public Map<String,Object> upiQr(@RequestParam double amount, @RequestParam String reference) {
        if (upiVpa == null || upiVpa.isBlank()) throw new IllegalStateException("PAYMENT_UPI_VPA is not configured");
        String encoded = java.net.URLEncoder.encode("upi://pay?pa=" + upiVpa + "&pn=" + merchantName + "&am=" + String.format(java.util.Locale.US, "%.2f", amount) + "&cu=INR&tn=MMT-" + reference, java.nio.charset.StandardCharsets.UTF_8);
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=320x320&margin=12&data=" + encoded;
        return Map.of("vpa", upiVpa, "amount", amount, "qrUrl", qrUrl, "upiUri", "upi://pay?pa=" + upiVpa + "&pn=" + merchantName + "&am=" + String.format(java.util.Locale.US, "%.2f", amount) + "&cu=INR&tn=MMT-" + reference);
    }
}
