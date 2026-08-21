package com.mmt.repository;
import com.mmt.model.Flight; import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.stereotype.Repository; import java.util.*;
@Repository public interface FlightRepository extends JpaRepository<Flight,Long> { List<Flight> findBySourceCityIgnoreCaseAndDestinationCityIgnoreCase(String sourceCity, String destinationCity); }
