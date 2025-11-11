package com.getwork.user.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class OtpGatewayService {

    private static final Logger log = LoggerFactory.getLogger(OtpGatewayService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String AUTH_SERVICE_URL = "http://localhost:8082/api/v1/auth/request-otp";

    public void sendOtp(String phone, String channel) {
        try {
            Map<String, String> body = Map.of(
                "destination", phone,
                "channel", channel
            );

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    AUTH_SERVICE_URL, body, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("OTP request initiated successfully for {}", phone);
            } else {
                log.warn("OTP request failed: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending OTP: {}", e.getMessage());
        }
    }
}
