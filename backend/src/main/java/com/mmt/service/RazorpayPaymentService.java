package com.mmt.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RazorpayPaymentService {
    private final RestTemplate rest = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();
    @Value("${razorpay.key-id:}") private String keyId;
    @Value("${razorpay.key-secret:}") private String keySecret;
    @Value("${razorpay.base-url:https://api.razorpay.com/v1}") private String baseUrl;

    public boolean configured() { return !blank(keyId) && !blank(keySecret); }
    public String keyId() { return keyId; }

    public JsonNode createOrder(double amountInr, String receipt, String notes) throws Exception {
        requireConfigured();
        HttpHeaders h = new HttpHeaders();
        h.setBasicAuth(keyId, keySecret);
        h.setContentType(MediaType.APPLICATION_JSON);
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("amount", Math.round(amountInr * 100));
        body.put("currency", "INR");
        body.put("receipt", receipt);
        body.put("payment_capture", 1);
        body.put("notes", Map.of("booking", notes));
        ResponseEntity<String> r = rest.postForEntity(baseUrl + "/orders", new HttpEntity<>(mapper.writeValueAsString(body), h), String.class);
        return mapper.readTree(r.getBody());
    }

    public JsonNode fetchPayment(String paymentId) throws Exception {
        requireConfigured();
        HttpHeaders h = authHeaders();
        ResponseEntity<String> r = rest.exchange(baseUrl + "/payments/" + paymentId, HttpMethod.GET, new HttpEntity<>(h), String.class);
        return mapper.readTree(r.getBody());
    }

    public JsonNode refund(String paymentId, double amountInr, String receipt) throws Exception {
        requireConfigured();
        HttpHeaders h = authHeaders(); h.setContentType(MediaType.APPLICATION_JSON);
        Map<String,Object> body = new LinkedHashMap<>();
        body.put("amount", Math.round(amountInr * 100));
        body.put("speed", "normal");
        body.put("receipt", receipt);
        ResponseEntity<String> r = rest.postForEntity(baseUrl + "/payments/" + paymentId + "/refund", new HttpEntity<>(mapper.writeValueAsString(body), h), String.class);
        return mapper.readTree(r.getBody());
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        if (!configured()) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal((orderId + "|" + paymentId).getBytes(StandardCharsets.UTF_8));
            return MessageDigestHex.equalsHex(digest, signature);
        } catch (Exception e) { return false; }
    }

    private HttpHeaders authHeaders() { HttpHeaders h = new HttpHeaders(); h.setBasicAuth(keyId, keySecret); return h; }
    private void requireConfigured() { if (!configured()) throw new IllegalStateException("Razorpay is not configured. Add RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET to backend/.env."); }
    private boolean blank(String s) { return s == null || s.isBlank(); }

    private static final class MessageDigestHex {
        static boolean equalsHex(byte[] bytes, String hex) {
            if (hex == null || hex.length() != bytes.length * 2) return false;
            int diff = 0;
            for (int i=0;i<bytes.length;i++) {
                int v = Integer.parseInt(hex.substring(i*2,i*2+2),16);
                diff |= (bytes[i] & 0xff) ^ v;
            }
            return diff == 0;
        }
    }
}
