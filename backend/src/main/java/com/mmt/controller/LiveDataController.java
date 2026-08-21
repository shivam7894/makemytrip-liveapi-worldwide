package com.mmt.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.service.AmadeusApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/live")
@CrossOrigin(origins = "*")
public class LiveDataController {
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    private final AmadeusApiService amadeus;

    @Value("${weather.api.base-url}") private String weatherUrl;
    @Value("${fx.api.base-url}") private String fxUrl;
    @Value("${aviation.api.key:}") private String aviationKey;
    @Value("${aviation.api.base-url:http://api.aviationstack.com/v1}") private String aviationBaseUrl;
    @Value("${amadeus.client.id:}") private String amadeusId;
    @Value("${mail.smtp.host:}") private String smtpHost;
    @Value("${mail.smtp.username:}") private String smtpUsername;
    @Value("${razorpay.key-id:}") private String razorpayKey;
    @Value("${razorpay.key-secret:}") private String razorpaySecret;
    @Value("${payment.upi-vpa:}") private String upiVpa;
    @Value("${payment.required:true}") private boolean paymentRequired;

    public LiveDataController(AmadeusApiService amadeus) { this.amadeus = amadeus; }

    @GetMapping("/weather")
    public JsonNode weather(@RequestParam double latitude, @RequestParam double longitude) throws Exception {
        String url = weatherUrl + "?latitude=" + latitude + "&longitude=" + longitude
                + "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,wind_direction_10m"
                + "&timezone=auto";
        return mapper.readTree(rest.getForObject(url, String.class));
    }

    @GetMapping("/fx")
    public JsonNode fx(@RequestParam(defaultValue = "INR") String base,
                       @RequestParam(defaultValue = "USD,EUR,GBP,AED") String symbols) throws Exception {
        return mapper.readTree(rest.getForObject(fxUrl + "?from=" + base + "&to=" + symbols, String.class));
    }

    /** Real Amadeus flight shopping. Requires Amadeus credentials in backend/.env. */
    @GetMapping("/amadeus/flights")
    public JsonNode amadeusFlights(@RequestParam String from, @RequestParam String to,
                                   @RequestParam String date,
                                   @RequestParam(defaultValue = "1") int adults) throws Exception {
        return amadeus.flightOffers(from.toUpperCase(), to.toUpperCase(), LocalDate.parse(date), adults);
    }

    /** Real Amadeus airport/city reference search. */
    @GetMapping("/amadeus/locations")
    public JsonNode amadeusLocations(@RequestParam String keyword) throws Exception {
        return amadeus.locationSearch(keyword);
    }

    @GetMapping("/amadeus/hotel-list")
    public JsonNode amadeusHotelList(@RequestParam String cityCode) throws Exception {
        return amadeus.hotelListByCity(cityCode.toUpperCase());
    }

    /** Real Amadeus hotel offers/inventory pricing. */
    @GetMapping("/amadeus/hotels")
    public JsonNode amadeusHotels(@RequestParam String hotelIds,
                                  @RequestParam String checkIn,
                                  @RequestParam String checkOut,
                                  @RequestParam(defaultValue = "1") int adults) throws Exception {
        return amadeus.hotelOffers(hotelIds, LocalDate.parse(checkIn), LocalDate.parse(checkOut), adults);
    }

    /** Real Aviationstack flight status for a specific flight number. */
    @GetMapping("/aviation/flight")
    public JsonNode aviationFlight(@RequestParam String flightIata) throws Exception {
        requireAviationKey();
        String url = aviationBaseUrl + "/flights?access_key=" + aviationKey
                + "&flight_iata=" + flightIata.replace("-", "").toUpperCase();
        return mapper.readTree(rest.getForObject(url, String.class));
    }

    /** Live Aviationstack route search. Requires AVIATION_API_KEY. */
    @GetMapping("/aviation/route")
    public JsonNode aviationRoute(@RequestParam String from, @RequestParam String to,
                                  @RequestParam(required=false) String date,
                                  @RequestParam(defaultValue="100") int limit) throws Exception {
        requireAviationKey();
        StringBuilder url=new StringBuilder(aviationBaseUrl).append("/flights?access_key=").append(aviationKey)
                .append("&dep_iata=").append(from.toUpperCase()).append("&arr_iata=").append(to.toUpperCase())
                .append("&limit=").append(Math.min(Math.max(limit,1),100));
        if(date!=null&&!date.isBlank()) url.append("&flight_date=").append(date);
        return mapper.readTree(rest.getForObject(url.toString(), String.class));
    }

    /** Real Aviationstack live airport arrivals/departures. */
    @GetMapping("/aviation/airport-board")
    public JsonNode aviationAirportBoard(@RequestParam String airportIata,
                                         @RequestParam(defaultValue = "arrival") String type,
                                         @RequestParam(defaultValue = "100") int limit) throws Exception {
        requireAviationKey();
        String normalizedType = "departure".equalsIgnoreCase(type) ? "departure" : "arrival";
        String url = aviationBaseUrl + "/flights?access_key=" + aviationKey
                + "&" + normalizedType + ".iata=" + airportIata.toUpperCase()
                + "&limit=" + Math.min(Math.max(limit, 1), 100);
        return mapper.readTree(rest.getForObject(url, String.class));
    }

    private void requireAviationKey() {
        if (aviationKey == null || aviationKey.isBlank()) {
            throw new IllegalStateException("AVIATION_API_KEY is not configured. Add it to backend/.env for real flight status.");
        }
    }

    @GetMapping("/providers")
    public Map<String, Object> providers() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("aviationstack", aviationKey != null && !aviationKey.isBlank());
        m.put("amadeus", amadeusId != null && !amadeusId.isBlank());
        m.put("openMeteo", true);
        m.put("frankfurterFx", true);
        m.put("smtp", smtpHost != null && !smtpHost.isBlank() && smtpUsername != null && !smtpUsername.isBlank());
        m.put("liveWebSocket", true);
        m.put("razorpay", razorpayKey != null && !razorpayKey.isBlank() && razorpaySecret != null && !razorpaySecret.isBlank());
        m.put("upiQr", upiVpa != null && !upiVpa.isBlank());
        m.put("paymentRequired", paymentRequired);
        m.put("liveFlightPollingSeconds", 20);
        m.put("liveMarketPollingSeconds", 15);
        return m;
    }
}
