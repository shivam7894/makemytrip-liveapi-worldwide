package com.mmt.repository;
import com.mmt.model.Booking; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface BookingRepository extends JpaRepository<Booking,Long> { List<Booking> findByUserIdOrderByBookingTimeDesc(Long userId);
 boolean existsByItemIdAndSeatNumberAndStatus(Long itemId, String seatNumber, String status);
 List<Booking> findByItemIdAndSeatNumberAndStatus(Long itemId, String seatNumber, String status); }
