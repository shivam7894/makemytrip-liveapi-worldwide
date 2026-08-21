package com.mmt.repository;
import com.mmt.model.TrackedFlight; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface TrackedFlightRepository extends JpaRepository<TrackedFlight,Long> { List<TrackedFlight> findByUserId(Long userId); void deleteByUserIdAndFlightId(Long userId,Long flightId); boolean existsByUserIdAndFlightId(Long userId,Long flightId); }
