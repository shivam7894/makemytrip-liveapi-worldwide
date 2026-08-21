package com.mmt.controller;

import com.mmt.model.Booking;
import com.mmt.model.Flight;
import com.mmt.model.HotelRoom;
import com.mmt.model.PriceFreeze;
import com.mmt.repository.BookingRepository;
import com.mmt.repository.FlightRepository;
import com.mmt.repository.HotelRepository;
import com.mmt.repository.HotelRoomRepository;
import com.mmt.repository.PriceFreezeRepository;
import com.mmt.repository.PaymentTransactionRepository;
import com.mmt.service.RazorpayPaymentService;

import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingRepository bookings;
    private final FlightRepository flights;
    private final HotelRepository hotels;
    private final HotelRoomRepository rooms;
    private final PriceFreezeRepository freezes;
    private final PaymentTransactionRepository payments;
    private final RazorpayPaymentService razorpay;
    @org.springframework.beans.factory.annotation.Value("${payment.required:true}") private boolean paymentRequired;

    public BookingController(
            BookingRepository b,
            FlightRepository f,
            HotelRepository h,
            HotelRoomRepository r,
            PriceFreezeRepository pf, PaymentTransactionRepository payments, RazorpayPaymentService razorpay) {

        this.bookings = b;
        this.flights = f;
        this.hotels = h;
        this.rooms = r;
        this.freezes = pf;
        this.payments = payments;
        this.razorpay = razorpay;
    }

    // =========================================================
    // GET ALL BOOKINGS / USER BOOKINGS
    // =========================================================

    @GetMapping
    public List<Booking> byUser(
            @RequestParam(required = false) Long userId) {

        if (userId == null) {
            return bookings.findAll();
        }

        return bookings.findByUserIdOrderByBookingTimeDesc(userId);
    }

    // =========================================================
    // CANCELLATION REASONS
    // =========================================================

    @GetMapping("/cancellation-reasons")
    public List<String> cancellationReasons() {

        return List.of(
                "Change of plans",
                "Found a better price",
                "Flight schedule changed",
                "Personal emergency",
                "Duplicate booking",
                "Hotel/room issue",
                "Other"
        );
    }

    // =========================================================
    // CREATE BOOKING
    // =========================================================

    @PostMapping
    public Booking create(@RequestBody Booking b) {

        if (paymentRequired && !razorpay.configured()) {
            throw new RuntimeException("Real payment is enabled but Razorpay is not configured. Add RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET to backend/.env before booking.");
        }

        // Default booking type
        if (b.getBookingType() == null ||
                b.getBookingType().isBlank()) {

            b.setBookingType("FLIGHT");
        }

        // -----------------------------------------------------
        // FLIGHT BOOKING
        // -----------------------------------------------------

        if ("FLIGHT".equalsIgnoreCase(b.getBookingType())) {

            if (b.getItemId() == null) {
                throw new RuntimeException(
                        "Flight ID is required for flight booking"
                );
            }

            Flight f = flights.findById(b.getItemId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Flight not found: " + b.getItemId()
                            )
                    );

            // Check seats
            if (f.getAvailableSeats() <= 0) {
                throw new RuntimeException(
                        "No seats available"
                );
            }

            // Check selected seat
            if (b.getSeatNumber() != null &&
                    !b.getSeatNumber().isBlank()) {

                boolean seatAlreadyBooked =
                        bookings.existsByItemIdAndSeatNumberAndStatus(
                                f.getId(),
                                b.getSeatNumber(),
                                "CONFIRMED"
                        );

                if (seatAlreadyBooked) {
                    throw new RuntimeException(
                            "Seat " + b.getSeatNumber()
                                    + " is already booked"
                    );
                }
            }

            // Decrease available seats
            f.setAvailableSeats(
                    f.getAvailableSeats() - 1
            );

            flights.save(f);

            // Current live price
            double livePrice = f.getPrice();

            // -------------------------------------------------
            // PRICE FREEZE
            // -------------------------------------------------

            var freeze = freezes
                    .findTopByUserIdAndItemTypeAndItemIdAndStatusOrderByExpiresAtDesc(
                            b.getUserId(),
                            "FLIGHT",
                            f.getId(),
                            "ACTIVE"
                    );

            double lockedPrice = freeze
                    .filter(x ->
                            x.getExpiresAt() != null &&
                            x.getExpiresAt()
                                    .isAfter(LocalDateTime.now())
                    )
                    .map(PriceFreeze::getLockedPrice)
                    .orElse(livePrice);

            // -------------------------------------------------
            // SEAT SURCHARGE
            // -------------------------------------------------

            double seatSurcharge = 0.0;

            if (b.getSeatNumber() != null &&
                    !b.getSeatNumber().isBlank()) {

                String seat = b.getSeatNumber().trim();

                if (seat.startsWith("1") ||
                        seat.startsWith("2")) {

                    seatSurcharge = 799.0;
                }
            }

            // Final flight amount
            b.setTotalAmount(
                    lockedPrice + seatSurcharge
            );
        }

        // -----------------------------------------------------
        // HOTEL BOOKING
        // -----------------------------------------------------

        else if ("HOTEL".equalsIgnoreCase(b.getBookingType())) {

            if (b.getRoomId() == null) {
                throw new RuntimeException(
                        "Room ID is required for hotel booking"
                );
            }

            HotelRoom r = rooms.findById(b.getRoomId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Hotel room not found: "
                                            + b.getRoomId()
                            )
                    );

            // Check room availability
            if (r.getAvailableRooms() <= 0) {
                throw new RuntimeException(
                        "Room unavailable"
                );
            }

            // Decrease available rooms
            r.setAvailableRooms(
                    r.getAvailableRooms() - 1
            );

            rooms.save(r);

            // If frontend didn't send amount,
            // calculate it from live room price
            if (b.getTotalAmount() == null) {

                double livePrice = r.getPrice();

                // -------------------------------------------------
                // HOTEL PRICE FREEZE
                // -------------------------------------------------

                var freeze = freezes
                        .findTopByUserIdAndItemTypeAndItemIdAndStatusOrderByExpiresAtDesc(
                                b.getUserId(),
                                "HOTEL",
                                r.getHotelId(),
                                "ACTIVE"
                        );

                double finalPrice = freeze
                        .filter(x ->
                                x.getExpiresAt() != null &&
                                x.getExpiresAt()
                                        .isAfter(LocalDateTime.now())
                        )
                        .map(PriceFreeze::getLockedPrice)
                        .orElse(livePrice);

                b.setTotalAmount(finalPrice);
            }
        }

        // -----------------------------------------------------
        // INVALID BOOKING TYPE
        // -----------------------------------------------------

        else {

            throw new RuntimeException(
                    "Invalid booking type: "
                            + b.getBookingType()
            );
        }

        // -----------------------------------------------------
        // FINAL BOOKING DATA
        // -----------------------------------------------------

        b.setStatus(paymentRequired ? "PENDING_PAYMENT" : "CONFIRMED");
        b.setPaymentStatus(paymentRequired ? "CREATED" : "NOT_REQUIRED");
        b.setRefundStatus("NOT_REQUESTED");

        b.setBookingTime(
                LocalDateTime.now()
        );

        return bookings.save(b);
    }

    // =========================================================
    // CANCEL BOOKING
    // =========================================================

    @PostMapping("/{id}/cancel")
    public Booking cancel(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        Booking b = bookings.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Booking not found: " + id
                        )
                );

        // Only confirmed bookings can be cancelled
        if (!"CONFIRMED".equalsIgnoreCase(b.getStatus())) {

            throw new RuntimeException(
                    "Booking is not cancellable"
            );
        }

        // Cancellation reason
        String reason =
                body != null
                        ? body.getOrDefault(
                                "reason",
                                "Change of plans"
                        )
                        : "Change of plans";

        // -----------------------------------------------------
        // REFUND CALCULATION
        // -----------------------------------------------------

        long hours = 0;

        if (b.getBookingTime() != null) {

            hours = Duration.between(
                    b.getBookingTime(),
                    LocalDateTime.now()
            ).toHours();
        }

        /*
         * Refund policy:
         *
         * Within 24 hours  -> 50%
         * After 24 hours   -> 20%
         */

        double refundPercentage;

        if (hours <= 24) {
            refundPercentage = 0.50;
        } else {
            refundPercentage = 0.20;
        }

        double originalAmount =
                b.getTotalAmount() != null
                        ? b.getTotalAmount()
                        : 0.0;

        double refund =
                Math.round(
                        originalAmount *
                                refundPercentage *
                                100.0
                ) / 100.0;

        // -----------------------------------------------------
        // UPDATE BOOKING
        // -----------------------------------------------------

        b.setStatus("CANCELLED");

        b.setCancellationReason(reason);

        b.setRefundAmount(refund);

        b.setRefundStatus("PENDING");
        b.setExpectedRefundDate(LocalDateTime.now().plusDays(3));

        if ("RAZORPAY".equalsIgnoreCase(b.getPaymentProvider()) && b.getPaymentId() != null && refund > 0 && razorpay.configured()) {
            try {
                var refundNode = razorpay.refund(b.getPaymentId(), refund, "MMT-REFUND-" + b.getId());
                b.setRefundStatus("PROCESSING");
                b.setPaymentStatus("REFUND_INITIATED");
                b.setExpectedRefundDate(LocalDateTime.now().plusDays(5));
                payments.findTopByBookingIdOrderByCreatedAtDesc(b.getId()).ifPresent(tx -> {
                    tx.setRefundId(refundNode.path("id").asText());
                    tx.setRefundAmount(refund);
                    tx.setStatus("REFUND_INITIATED");
                    tx.setUpdatedAt(LocalDateTime.now());
                    payments.save(tx);
                });
            } catch (Exception ex) {
                b.setRefundStatus("PENDING");
            }
        }

        b.setCancelledAt(
                LocalDateTime.now()
        );

        // -----------------------------------------------------
        // RESTORE FLIGHT SEAT
        // -----------------------------------------------------

        if ("FLIGHT".equalsIgnoreCase(
                b.getBookingType())) {

            if (b.getItemId() != null) {

                flights.findById(b.getItemId())
                        .ifPresent(f -> {

                            f.setAvailableSeats(
                                    f.getAvailableSeats() + 1
                            );

                            flights.save(f);
                        });
            }
        }

        // -----------------------------------------------------
        // RESTORE HOTEL ROOM
        // -----------------------------------------------------

        if ("HOTEL".equalsIgnoreCase(
                b.getBookingType())) {

            if (b.getRoomId() != null) {

                rooms.findById(b.getRoomId())
                        .ifPresent(r -> {

                            r.setAvailableRooms(
                                    r.getAvailableRooms() + 1
                            );

                            rooms.save(r);
                        });
            }
        }

        // Save cancellation
        return bookings.save(b);
    }
}