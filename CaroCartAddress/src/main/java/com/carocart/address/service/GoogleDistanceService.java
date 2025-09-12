package com.carocart.address.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GoogleDistanceService {
    private final WebClient webClient;

 // GoogleDistanceService.java
    @Value("${google.maps.apiKey}")
    private String apiKey;

    // Optional: switch to two_wheeler where supported by Google in your region
    @Value("${google.maps.mode:driving}")
    private String travelMode; // driving | two_wheeler | bicycling | walking

    public GoogleDistanceService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://maps.googleapis.com").build();
    }

    public Double drivingKm(double oLat, double oLng, double dLat, double dLng) {
        String url = String.format(
            "/maps/api/distancematrix/json?origins=%f,%f&destinations=%f,%f&mode=%s&units=metric&key=%s",
            oLat, oLng, dLat, dLng, travelMode, apiKey
        );

        DistanceMatrixResponse resp = webClient.get()
            .uri(url)
            .retrieve()
            .onStatus(
                HttpStatusCode::isError,
                r -> r.bodyToMono(String.class)
                      .map(body -> new RuntimeException("Google Distance Matrix error: " + body))
            )
            .bodyToMono(DistanceMatrixResponse.class)
            .block();

        // Defensive validation
        if (resp == null || resp.rows == null || resp.rows.isEmpty()
            || resp.rows.get(0) == null
            || resp.rows.get(0).elements == null || resp.rows.get(0).elements.isEmpty()
            || resp.rows.get(0).elements.get(0) == null
            || resp.rows.get(0).elements.get(0).distance == null) {
            throw new RuntimeException("Invalid Distance Matrix response (missing distance)");
        }

        long meters = resp.rows.get(0).elements.get(0).distance.value;
        if (meters <= 0) {
            throw new RuntimeException("Distance Matrix returned non-positive distance");
        }
        return meters / 1000.0;
    }

    // --- Minimal DTOs for parsing the response ---
    static class DistanceMatrixResponse {
        public java.util.List<Row> rows;
        static class Row { public java.util.List<Element> elements; }
        static class Element { public ValueText distance; public ValueText duration; }
        static class ValueText { public long value; public String text; }
    }
}
