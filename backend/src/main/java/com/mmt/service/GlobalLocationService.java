package com.mmt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.model.LocationRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
public class GlobalLocationService {
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final AmadeusApiService amadeus;
    @Value("${aviation.api.key:}") private String aviationKey;
    @Value("${aviation.api.base-url:http://api.aviationstack.com/v1}") private String aviationBaseUrl;
    private final Map<String,Long> lastNominatimCall = new HashMap<>();

    public GlobalLocationService(AmadeusApiService amadeus) { this.amadeus = amadeus; }

    public List<LocationRecord> nominatim(String q) throws Exception {
        if (q == null || q.isBlank()) return List.of();
        synchronized (lastNominatimCall) {
            long now = System.currentTimeMillis(); long prev = lastNominatimCall.getOrDefault("global", 0L);
            if (now - prev < 900) Thread.sleep(900 - (now - prev));
            lastNominatimCall.put("global", System.currentTimeMillis());
        }
        String url = UriComponentsBuilder.fromHttpUrl("https://nominatim.openstreetmap.org/search")
                .queryParam("format","jsonv2").queryParam("addressdetails",1).queryParam("limit",20).queryParam("q",q).toUriString();
        HttpHeaders h = new HttpHeaders(); h.set("User-Agent","MMT-Travel-Platform/2.0"); h.setAccept(List.of(MediaType.APPLICATION_JSON));
        String body = rest.exchange(url, HttpMethod.GET, new HttpEntity<>(h), String.class).getBody();
        JsonNode arr = mapper.readTree(body);
        List<LocationRecord> out = new ArrayList<>();
        if (!arr.isArray()) return out;
        for (JsonNode n : arr) {
            JsonNode a=n.path("address");
            String type=n.path("type").asText("LOCATION").toUpperCase();
            if (type.contains("AIRPORT") || n.path("class").asText("").equalsIgnoreCase("aeroway")) type="AIRPORT";
            out.add(LocationRecord.builder().type(type).name(n.path("display_name").asText(n.path("name").asText(q)))
                    .city(first(a,"city","town","village","municipality","county"))
                    .state(first(a,"state","region"))
                    .district(first(a,"state_district","county"))
                    .country(a.path("country").asText(null)).code(a.path("ISO3166-2-lvl4").asText(null))
                    .latitude(number(n.path("lat").asText())).longitude(number(n.path("lon").asText())).build());
        }
        return out;
    }

    public List<LocationRecord> aviationAirports(String q) throws Exception {
        if (aviationKey == null || aviationKey.isBlank()) return List.of();
        StringBuilder u=new StringBuilder(aviationBaseUrl).append("/airports?access_key=").append(aviationKey).append("&limit=100");
        if(q!=null&&!q.isBlank()) u.append("&search=").append(java.net.URLEncoder.encode(q, java.nio.charset.StandardCharsets.UTF_8));
        JsonNode arr=mapper.readTree(rest.getForObject(u.toString(),String.class)).path("data");
        List<LocationRecord> out=new ArrayList<>();
        if(arr.isArray()) for(JsonNode n:arr) out.add(LocationRecord.builder().type("AIRPORT").name(n.path("airport_name").asText(n.path("name").asText("Airport"))).city(n.path("city_iata_code").asText(n.path("city_name").asText(null))).code(n.path("iata_code").asText(null)).country(n.path("country_name").asText(null)).latitude(number(n.path("latitude").asText())).longitude(number(n.path("longitude").asText())).build());
        return out;
    }



    public String nearestAirportCode(String location) throws Exception {
        if (location == null || location.isBlank()) return null;
        String geocodeUrl = UriComponentsBuilder.fromHttpUrl("https://nominatim.openstreetmap.org/search").queryParam("format","jsonv2").queryParam("limit",1).queryParam("q",location).toUriString();
        HttpHeaders h = new HttpHeaders(); h.set("User-Agent","MMT-Travel-Platform/2.0"); h.setAccept(List.of(MediaType.APPLICATION_JSON));
        JsonNode geo=mapper.readTree(rest.exchange(geocodeUrl,HttpMethod.GET,new HttpEntity<>(h),String.class).getBody());
        if(!geo.isArray()||geo.isEmpty()) return null;
        double lat=geo.get(0).path("lat").asDouble(), lon=geo.get(0).path("lon").asDouble();
        String q="[out:json][timeout:15];nwr[\"aeroway\"=\"aerodrome\"](around:200000,"+lat+","+lon+");out center tags;";
        String url="https://overpass-api.de/api/interpreter?data="+java.net.URLEncoder.encode(q,java.nio.charset.StandardCharsets.UTF_8);
        JsonNode root=mapper.readTree(rest.exchange(url,HttpMethod.GET,new HttpEntity<>(h),String.class).getBody());
        double best=Double.MAX_VALUE; String bestCode=null;
        for(JsonNode n:root.path("elements")){ JsonNode t=n.path("tags"); String iata=t.path("iata").asText("").trim(); if(iata.isBlank()) continue; double la=n.path("lat").asDouble(n.path("center").path("lat").asDouble()), lo=n.path("lon").asDouble(n.path("center").path("lon").asDouble()); double d=(la-lat)*(la-lat)+(lo-lon)*(lo-lon); if(d<best){best=d;bestCode=iata.toUpperCase();} }
        return bestCode;
    }

    /** Worldwide hotel discovery from OpenStreetMap. This is discovery data only; it is not bookable inventory. */
    public List<Map<String,Object>> hotelsNear(String location) throws Exception {
        if (location == null || location.isBlank()) return List.of();
        String geocodeUrl = UriComponentsBuilder.fromHttpUrl("https://nominatim.openstreetmap.org/search")
                .queryParam("format","jsonv2").queryParam("limit",1).queryParam("q",location).toUriString();
        HttpHeaders h = new HttpHeaders();
        h.set("User-Agent","MMT-Travel-Platform/2.0");
        h.setAccept(List.of(MediaType.APPLICATION_JSON));
        JsonNode geo = mapper.readTree(rest.exchange(geocodeUrl, HttpMethod.GET, new HttpEntity<>(h), String.class).getBody());
        if (!geo.isArray() || geo.isEmpty()) return List.of();
        double lat = geo.get(0).path("lat").asDouble();
        double lon = geo.get(0).path("lon").asDouble();
        String around = "[out:json][timeout:20];(nwr[\"tourism\"=\"hotel\"](around:25000,"+lat+","+lon+");nwr[\"tourism\"=\"motel\"](around:25000,"+lat+","+lon+");nwr[\"tourism\"=\"hostel\"](around:25000,"+lat+","+lon+"););out center tags;";
        String overpassUrl = "https://overpass-api.de/api/interpreter?data=" + java.net.URLEncoder.encode(around, java.nio.charset.StandardCharsets.UTF_8);
        JsonNode root = mapper.readTree(rest.exchange(overpassUrl, HttpMethod.GET, new HttpEntity<>(h), String.class).getBody());
        List<Map<String,Object>> out = new ArrayList<>();
        JsonNode elements=root.path("elements");
        if(elements.isArray()) for(JsonNode n:elements) {
            JsonNode tags=n.path("tags");
            String name=tags.path("name").asText("").trim();
            if(name.isBlank()) continue;
            double la=n.path("lat").asDouble(n.path("center").path("lat").asDouble(lat));
            double lo=n.path("lon").asDouble(n.path("center").path("lon").asDouble(lon));
            Map<String,Object> m=new LinkedHashMap<>();
            m.put("id", "OSM-"+n.path("type").asText("x")+"-"+n.path("id").asText());
            m.put("external", true); m.put("source", "OpenStreetMap"); m.put("bookable", false);
            m.put("name", name); m.put("city", tags.path("addr:city").asText(location));
            m.put("address", address(tags)); m.put("rating", tags.path("stars").asDouble(0));
            m.put("reviewCount", 0); m.put("basePrice", null); m.put("currentPrice", null);
            m.put("imageUrl", "https://images.unsplash.com/photo-1566073771259-6a8506099945?w=800&auto=format&fit=crop&q=80");
            m.put("amenities", amenities(tags)); m.put("description", "Worldwide hotel discovery result. Live room rates and booking require a connected hotel inventory provider.");
            m.put("latitude", la); m.put("longitude", lo); out.add(m);
        }
        return out.stream().limit(100).toList();
    }
    private static String address(JsonNode t){
        List<String> p=new ArrayList<>(); for(String k:new String[]{"addr:housenumber","addr:street","addr:city","addr:state","addr:country"}){String v=t.path(k).asText(""); if(!v.isBlank()) p.add(v);} return String.join(", ",p);
    }
    private static String amenities(JsonNode t){
        List<String> p=new ArrayList<>(); if(!t.path("internet_access").asText("").isBlank())p.add("Wi-Fi"); if(!t.path("swimming_pool").asText("").isBlank())p.add("Pool"); if(!t.path("restaurant").asText("").isBlank())p.add("Restaurant"); if(p.isEmpty())p.add("Hotel"); return String.join(",",p);
    }

    public List<LocationRecord> amadeusLocations(String q) throws Exception {
        if (!amadeus.configured()) return List.of();
        JsonNode arr=amadeus.locationSearch(q).path("data"); List<LocationRecord> out=new ArrayList<>();
        if(arr.isArray()) for(JsonNode n:arr) out.add(LocationRecord.builder().type(n.path("subType").asText("LOCATION")).name(n.path("name").asText(q)).city(n.path("address").path("cityName").asText(null)).code(n.path("iataCode").asText(null)).country(n.path("address").path("countryName").asText(null)).latitude(number(n.path("geoCode").path("latitude").asText())).longitude(number(n.path("geoCode").path("longitude").asText())).build());
        return out;
    }
    private static String first(JsonNode n,String... keys){for(String k:keys){String v=n.path(k).asText("");if(!v.isBlank())return v;}return null;}
    private static Double number(String s){try{return s==null||s.isBlank()?null:Double.valueOf(s);}catch(Exception e){return null;}}
}
