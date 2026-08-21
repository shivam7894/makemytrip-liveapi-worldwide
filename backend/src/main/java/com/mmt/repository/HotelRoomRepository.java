package com.mmt.repository;
import com.mmt.model.HotelRoom; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface HotelRoomRepository extends JpaRepository<HotelRoom,Long> { List<HotelRoom> findByHotelId(Long hotelId); }
