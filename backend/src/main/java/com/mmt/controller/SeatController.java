package com.mmt.controller;
import com.mmt.model.Booking;
import com.mmt.model.Flight;
import com.mmt.repository.BookingRepository;
import com.mmt.repository.FlightRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController @RequestMapping("/api/seats") @CrossOrigin(origins="*")
public class SeatController {
 private final FlightRepository flights; private final BookingRepository bookings;
 public SeatController(FlightRepository f, BookingRepository b){flights=f;bookings=b;}
 @GetMapping("/{flightId}") public Map<String,Object> map(@PathVariable Long flightId){
   Flight f=flights.findById(flightId).orElseThrow();
   List<String> seats=new ArrayList<>(); for(int r=1;r<=Math.max(1,(f.getTotalSeats()==null?60:f.getTotalSeats())/6);r++) for(char c='A';c<='F';c++) seats.add(r+""+c);
   Set<String> occupied=new HashSet<>();
   for(Booking b: bookings.findAll()) if("CONFIRMED".equalsIgnoreCase(b.getStatus()) && flightId.equals(b.getItemId()) && b.getSeatNumber()!=null) occupied.add(b.getSeatNumber());
   return Map.of("flightId",flightId,"totalSeats",f.getTotalSeats(),"availableSeats",f.getAvailableSeats(),"seats",seats,"occupied",occupied);
 }
}
