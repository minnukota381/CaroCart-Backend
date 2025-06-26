// src/main/java/com/carocart/address/util/MapmyIndiaTokenService.java

package com.carocart.address.util;

import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import java.util.Map;

@Component
public class MapmyIndiaTokenService {
    private final String clientId;
    private final String clientSecret;
    private String token;
    private long tokenExpiry = 0; // epoch millis

    public MapmyIndiaTokenService(
        @Value("${mapmyindia.rest.api.client_id}") String clientId,
        @Value("${mapmyindia.rest.api.client_secret}") String clientSecret
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public synchronized String getAccessToken() {
        if (token == null || System.currentTimeMillis() > tokenExpiry) {
            fetchToken();
        }
        return token;
    }

    private void fetchToken() {
        String url = "https://outpost.mapmyindia.com/api/security/oauth/token";
        String body = "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            Map<String, Object> respBody = response.getBody();
            if (respBody != null && respBody.get("access_token") != null) {
                token = (String) respBody.get("access_token");
                int expiresIn = ((Number) respBody.get("expires_in")).intValue();
                tokenExpiry = System.currentTimeMillis() + (expiresIn - 30) * 1000L; // 30s buffer
            } else {
                throw new RuntimeException("MapmyIndia token fetch failed: " + respBody);
            }
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("MapmyIndia token fetch error: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("MapmyIndia token fetch error: " + e.getMessage(), e);
        }
    }
}
