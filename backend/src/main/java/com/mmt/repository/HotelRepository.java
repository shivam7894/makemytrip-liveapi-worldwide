package com.mmt.repository;
import com.mmt.model.Hotel; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface HotelRepository extends JpaRepository<Hotel,Long> { List<Hotel> findByCityContainingIgnoreCase(String city); }
