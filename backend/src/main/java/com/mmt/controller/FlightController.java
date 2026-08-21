package com.mmt.controller;
import com.mmt.model.*; import com.mmt.repository.*; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import java.time.*; import java.util.*;
@RestController @RequestMapping("/api/flights") @CrossOrigin(origins="*") public class FlightController { private final FlightRepository flights; private final TrackedFlightRepository tracked; private final PriceHistoryRepository history; private final FlightStatusUpdateRepository updates; private final PriceFreezeRepository freezes;
 public FlightController(FlightRepository f,TrackedFlightRepository t,PriceHistoryRepository h,FlightStatusUpdateRepository u,PriceFreezeRepository p){flights=f;tracked=t;history=h;updates=u;freezes=p;}
 @GetMapping public List<Flight> all(){return flights.findAll();}
 @GetMapping("/search") public List<Flight> search(@RequestParam String from,@RequestParam String to){return flights.findBySourceCityIgnoreCaseAndDestinationCityIgnoreCase(from,to);}
 @GetMapping("/{id}") public Flight one(@PathVariable Long id){return flights.findById(id).orElseThrow();}
 @GetMapping("/{id}/history") public List<PriceHistory> priceHistory(@PathVariable Long id){return history.findByItemTypeAndItemIdOrderByRecordedAtAsc("FLIGHT",id);}
 @GetMapping("/{id}/status-history") public List<FlightStatusUpdate> statusHistory(@PathVariable Long id){return updates.findByFlightIdOrderByUpdatedAtDesc(id);}
 @PostMapping("/{id}/track") public Map<String,Object> track(@PathVariable Long id,@RequestParam Long userId){ if(!tracked.existsByUserIdAndFlightId(userId,id)) tracked.save(TrackedFlight.builder().userId(userId).flightId(id).createdAt(LocalDateTime.now()).build()); return Map.of("tracked",true); }
 @DeleteMapping("/{id}/track") public Map<String,Object> untrack(@PathVariable Long id,@RequestParam Long userId){tracked.deleteByUserIdAndFlightId(userId,id); return Map.of("tracked",false);}
 @GetMapping("/tracked") public List<Flight> tracked(@RequestParam Long userId){List<Flight> out=new ArrayList<>(); for(TrackedFlight t:tracked.findByUserId(userId)) flights.findById(t.getFlightId()).ifPresent(out::add); return out;}
 @PostMapping("/{id}/freeze") public PriceFreeze freeze(@PathVariable Long id,@RequestParam Long userId,@RequestParam(defaultValue="60") int minutes){Flight f=one(id); PriceFreeze p=PriceFreeze.builder().userId(userId).itemType("FLIGHT").itemId(id).lockedPrice(f.getPrice()).expiresAt(LocalDateTime.now().plusMinutes(minutes)).status("ACTIVE").build(); return freezes.save(p);}
 @PostMapping("/freeze") public PriceFreeze freezeBody(@RequestBody Map<String,Object> b){
   Long id=Long.valueOf(b.get("flightId").toString()); Long userId=Long.valueOf(b.get("userId").toString());
   return freeze(id,userId,2880);
 }
 @GetMapping("/freeze/user/{userId}") public List<PriceFreeze> userFreezes(@PathVariable Long userId){ return freezes.findByUserIdAndStatus(userId,"ACTIVE"); }
 @GetMapping("/{id}/updates") public List<FlightStatusUpdate> updatesAlias(@PathVariable Long id){ return updates.findByFlightIdOrderByUpdatedAtDesc(id); }

}
