package com.carocart.authentication.service;

import com.carocart.authentication.dto.DrivingInfo;
import com.carocart.authentication.entity.Vendor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class DistanceMatrixService {

    private final WebClient webClient;

    @Value("${google.maps.apiKey}")
    private String apiKey;

    // driving | two_wheeler | bicycling | walking (two_wheeler only in some regions)
    @Value("${google.maps.mode:driving}")
    private String travelMode;

    public DistanceMatrixService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://maps.googleapis.com").build();
    }

    /**
     * Calls Google Distance Matrix to get driving distance & duration
     * for a single origin against many vendor destinations (batched).
     *
     * @return Map of vendorId -> DrivingInfo(km, minutes)
     */
    public Map<Long, DrivingInfo> drivingForVendors(double originLat, double originLng, List<Vendor> vendors) {
        Map<Long, DrivingInfo> out = new HashMap<>();
        if (vendors == null || vendors.isEmpty()) return out;

        // Google classic DM API allows up to 25 destinations per request
        final int BATCH_SIZE = 25;

        for (int i = 0; i < vendors.size(); i += BATCH_SIZE) {
            List<Vendor> batch = vendors.subList(i, Math.min(i + BATCH_SIZE, vendors.size()));

            // Build destinations string "lat,lng|lat,lng|..."
            String destinations = batch.stream()
                    .filter(v -> v.getLatitude() != null && v.getLongitude() != null)
                    .map(v -> v.getLatitude() + "," + v.getLongitude())
                    .collect(Collectors.joining("|"));

            if (destinations.isBlank()) continue;

            DistanceMatrixResponse resp = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/maps/api/distancematrix/json")
                            .queryParam("origins", originLat + "," + originLng)
                            .queryParam("destinations", destinations)
                            .queryParam("mode", travelMode)
                            .queryParam("units", "metric")
                            .queryParam("key", apiKey)
                            .build())
                    .retrieve()
                    .onStatus(
                            HttpStatusCode::isError,
                            r -> r.bodyToMono(String.class)
                                  .map(body -> new RuntimeException("Google Distance Matrix error: " + body))
                    )
                    .bodyToMono(DistanceMatrixResponse.class)
                    .block();

            if (resp == null || resp.rows == null || resp.rows.isEmpty()) continue;
            List<DistanceMatrixResponse.Element> elements = resp.rows.get(0).elements;
            if (elements == null || elements.isEmpty()) continue;

            // Map elements back to vendors in order
            for (int j = 0; j < batch.size() && j < elements.size(); j++) {
                Vendor v = batch.get(j);
                DistanceMatrixResponse.Element el = elements.get(j);
                if (el == null || !"OK".equals(el.status) || el.distance == null || el.duration == null) continue;

                long meters = el.distance.value;
                long seconds = el.duration.value;
                if (meters <= 0 || seconds <= 0) continue;

                double km = meters / 1000.0;
                int minutes = (int) Math.ceil(seconds / 60.0);
                out.put(v.getId(), new DrivingInfo(km, minutes));
            }
        }

        return out;
    }

    // --- Minimal response DTOs for parsing ---
    static class DistanceMatrixResponse {
        public String status;
        public List<Row> rows;
        static class Row { public List<Element> elements; }
        static class Element {
            public String status;
            public ValueText distance;
            public ValueText duration;
        }
        static class ValueText { public long value; public String text; }
    }
}
