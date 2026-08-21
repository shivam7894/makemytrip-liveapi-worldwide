package com.mmt.controller;

import com.mmt.model.LocationRecord;
import com.mmt.repository.LocationRepository;
import com.mmt.service.GlobalLocationService;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins="*")
public class LocationController {
    private final LocationRepository repo;
    private final GlobalLocationService global;
    public LocationController(LocationRepository r, GlobalLocationService g){repo=r;global=g;}

    @GetMapping("/search")
    public List<LocationRecord> search(@RequestParam(defaultValue="") String q) {
        if(q.isBlank()) return repo.findAll().stream().limit(100).toList();
        LinkedHashMap<String,LocationRecord> out=new LinkedHashMap<>();
        repo.findTop100ByNameContainingIgnoreCaseOrCityContainingIgnoreCase(q,q).forEach(x->out.put(key(x),x));
        try { global.amadeusLocations(q).forEach(x->out.putIfAbsent(key(x),x)); } catch(Exception ignored) {}
        try { global.aviationAirports(q).forEach(x->out.putIfAbsent(key(x),x)); } catch(Exception ignored) {}
        if(out.size()<20) try { global.nominatim(q).forEach(x->out.putIfAbsent(key(x),x)); } catch(Exception ignored) {}
        return out.values().stream().limit(50).toList();
    }

    @GetMapping("/global")
    public List<LocationRecord> global(@RequestParam String q){
        LinkedHashMap<String,LocationRecord> out=new LinkedHashMap<>();
        try { global.amadeusLocations(q).forEach(x->out.put(key(x),x)); } catch(Exception ignored) {}
        try { global.aviationAirports(q).forEach(x->out.putIfAbsent(key(x),x)); } catch(Exception ignored) {}
        try { global.nominatim(q).forEach(x->out.putIfAbsent(key(x),x)); } catch(Exception ignored) {}
        return out.values().stream().limit(100).toList();
    }

    @GetMapping("/resolve-airport") public Map<String,String> resolveAirport(@RequestParam String q) throws Exception { String code=global.nearestAirportCode(q); return code==null?Map.of():Map.of("iataCode",code); }
    @GetMapping("/airports")
    public List<LocationRecord> airports(@RequestParam(required=false) String q){
        LinkedHashMap<String,LocationRecord> out=new LinkedHashMap<>();
        repo.findByType("AIRPORT").stream().filter(x->q==null||q.isBlank()||(x.getName()+" "+x.getCity()+" "+x.getCode()+" "+x.getState()).toLowerCase().contains(q.toLowerCase())).limit(100).forEach(x->out.put(key(x),x));
        try { global.aviationAirports(q).forEach(x->out.putIfAbsent(key(x),x)); } catch(Exception ignored) {}
        try { if(q!=null&&!q.isBlank()) global.amadeusLocations(q).stream().filter(x->"AIRPORT".equalsIgnoreCase(x.getType())).forEach(x->out.putIfAbsent(key(x),x)); } catch(Exception ignored) {}
        return out.values().stream().limit(100).toList();
    }
    @GetMapping("/states") public List<LocationRecord> states(){return repo.findByType("STATE");}
    @GetMapping("/districts") public List<LocationRecord> districts(@RequestParam(required=false) String state){return repo.findByType("DISTRICT").stream().filter(x->state==null||state.isBlank()||x.getState().equalsIgnoreCase(state)).toList();}
    private String key(LocationRecord x){return (x.getType()+"|"+(x.getCode()==null?"":x.getCode())+"|"+(x.getName()==null?"":x.getName())).toLowerCase();}
}
