package com.mmt.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthController {
    @Value("${mail.smtp.host:}") private String smtpHost;
    @Value("${mail.smtp.username:}") private String smtpUsername;
    @Value("${aviation.api.key:}") private String aviationKey;
    @Value("${amadeus.client.id:}") private String amadeusId;
    @Value("${razorpay.key-id:}") private String razorpayKey;
    @Value("${razorpay.key-secret:}") private String razorpaySecret;

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("status", "UP");
        out.put("timestamp", Instant.now().toString());
        out.put("smtpConfigured", !smtpHost.isBlank() && !smtpUsername.isBlank());
        out.put("aviationstackConfigured", !aviationKey.isBlank());
        out.put("amadeusConfigured", !amadeusId.isBlank());
        out.put("database", "configured");
        out.put("razorpayConfigured", !razorpayKey.isBlank() && !razorpaySecret.isBlank());
        return out;
    }
}
