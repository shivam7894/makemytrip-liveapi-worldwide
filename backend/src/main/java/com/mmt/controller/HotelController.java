package com.mmt.controller;
import com.mmt.model.*; import com.mmt.repository.*; import com.mmt.service.GlobalLocationService; import org.springframework.web.bind.annotation.*; import java.time.*; import java.util.*;
@RestController @RequestMapping("/api/hotels") @CrossOrigin(origins="*") public class HotelController { private final HotelRepository hotels; private final HotelRoomRepository rooms; private final PriceHistoryRepository history; private final PriceFreezeRepository freezes; private final GlobalLocationService global;
 public HotelController(HotelRepository h,HotelRoomRepository r,PriceHistoryRepository ph,PriceFreezeRepository pf, GlobalLocationService g){hotels=h;rooms=r;history=ph;freezes=pf;global=g;}
 @GetMapping public List<Hotel> all(@RequestParam(required=false,defaultValue="") String city){return city.isBlank()?hotels.findAll():hotels.findByCityContainingIgnoreCase(city);}
 @GetMapping("/search") public List<Hotel> search(@RequestParam(defaultValue="") String city){ return all(city); }
 @GetMapping("/global") public List<Map<String,Object>> global(@RequestParam String location) throws Exception { return global.hotelsNear(location); }
 @GetMapping("/{id}/rooms") public List<HotelRoom> rooms(@PathVariable Long id){return rooms.findByHotelId(id);}
 @GetMapping("/{id}/history") public List<PriceHistory> history(@PathVariable Long id){return history.findByItemTypeAndItemIdOrderByRecordedAtAsc("HOTEL",id);}
 @PostMapping("/{id}/freeze") public PriceFreeze freeze(@PathVariable Long id,@RequestParam Long userId,@RequestParam(defaultValue="60") int minutes){Hotel h=hotels.findById(id).orElseThrow(); return freezes.save(PriceFreeze.builder().userId(userId).itemType("HOTEL").itemId(id).lockedPrice(h.getCurrentPrice()).expiresAt(LocalDateTime.now().plusMinutes(minutes)).status("ACTIVE").build());}
}
