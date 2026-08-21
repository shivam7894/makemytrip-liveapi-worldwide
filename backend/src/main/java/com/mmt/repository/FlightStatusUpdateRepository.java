package com.mmt.repository;
import com.mmt.model.FlightStatusUpdate; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface FlightStatusUpdateRepository extends JpaRepository<FlightStatusUpdate,Long> { List<FlightStatusUpdate> findByFlightIdOrderByUpdatedAtDesc(Long flightId); }
