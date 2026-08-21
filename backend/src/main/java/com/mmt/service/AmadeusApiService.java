package com.mmt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;

@Service
public class AmadeusApiService {
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    @Value("${amadeus.client.id:}") private String clientId;
    @Value("${amadeus.client.secret:}") private String clientSecret;
    @Value("${amadeus.base-url:https://test.api.amadeus.com}") private String baseUrl;
    private volatile String token;
    private volatile long tokenExpiresAt;

    public boolean configured(){ return clientId != null && !clientId.isBlank() && clientSecret != null && !clientSecret.isBlank(); }

    private synchronized String token() throws Exception {
        if (!configured()) throw new IllegalStateException("Amadeus credentials are not configured");
        if (token != null && System.currentTimeMillis() < tokenExpiresAt) return token;
        HttpHeaders h = new HttpHeaders(); h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String,String> form = new LinkedMultiValueMap<>();
        form.add("grant_type","client_credentials"); form.add("client_id",clientId); form.add("client_secret",clientSecret);
        ResponseEntity<String> r = rest.postForEntity(baseUrl + "/v1/security/oauth2/token", new HttpEntity<>(form,h), String.class);
        JsonNode n = mapper.readTree(r.getBody()); token=n.path("access_token").asText();
        tokenExpiresAt=System.currentTimeMillis() + Math.max(60,n.path("expires_in").asLong(1800)-60)*1000L;
        return token;
    }

    private JsonNode get(String url) throws Exception {
        HttpHeaders h = new HttpHeaders(); h.setBearerAuth(token());
        ResponseEntity<String> r = rest.exchange(url, HttpMethod.GET, new HttpEntity<>(h), String.class);
        return mapper.readTree(r.getBody());
    }

    public JsonNode locationSearch(String keyword) throws Exception {
        String url= UriComponentsBuilder.fromHttpUrl(baseUrl+"/v1/reference-data/locations")
          .queryParam("subType","AIRPORT,CITY")
          .queryParam("keyword",keyword)
          .queryParam("page[limit]",20).toUriString();
        return get(url);
    }

    public JsonNode flightOffers(String from,String to,LocalDate date,int adults) throws Exception {
        String url= UriComponentsBuilder.fromHttpUrl(baseUrl+"/v2/shopping/flight-offers")
          .queryParam("originLocationCode",from).queryParam("destinationLocationCode",to)
          .queryParam("departureDate",date).queryParam("adults",adults).queryParam("currencyCode","INR").queryParam("max",20).toUriString();
        return get(url);
    }

    public JsonNode hotelListByCity(String cityCode) throws Exception {
        String url=UriComponentsBuilder.fromHttpUrl(baseUrl+"/v1/reference-data/locations/hotels/by-city")
          .queryParam("cityCode",cityCode).queryParam("radius",50).queryParam("radiusUnit","KM").queryParam("hotelSource","ALL").toUriString();
        return get(url);
    }

    public JsonNode hotelOffers(String hotelIds,LocalDate checkIn,LocalDate checkOut,int adults) throws Exception {
        String url=UriComponentsBuilder.fromHttpUrl(baseUrl+"/v3/shopping/hotel-offers")
          .queryParam("hotelIds",hotelIds).queryParam("adults",adults).queryParam("checkInDate",checkIn)
          .queryParam("checkOutDate",checkOut).queryParam("roomQuantity",1).toUriString();
        return get(url);
    }
}
