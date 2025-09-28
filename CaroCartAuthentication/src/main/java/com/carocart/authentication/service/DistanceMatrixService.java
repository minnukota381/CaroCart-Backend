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

    // driving | two_wheeler | bicycling | walking
    @Value("${google.maps.mode:driving}")
    private String travelMode;

    public DistanceMatrixService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://maps.googleapis.com").build();
    }

    /**
     * Calls Google Distance Matrix for one origin against many vendor destinations (batched).
     * Returns vendorId -> DrivingInfo(km, minutes).
     */
    public Map<Long, DrivingInfo> drivingForVendors(double originLat, double originLng, List<Vendor> vendors) {
        Map<Long, DrivingInfo> out = new HashMap<>();
        if (vendors == null || vendors.isEmpty()) return out;

        final int BATCH_SIZE = 25;

        for (int i = 0; i < vendors.size(); i += BATCH_SIZE) {
            List<Vendor> batch = vendors.subList(i, Math.min(i + BATCH_SIZE, vendors.size()));

            // ✅ Build a filtered list ONCE and reuse it for both query + mapping
            List<Vendor> destVendors = batch.stream()
                    .filter(v -> v.getLatitude() != null && v.getLongitude() != null)
                    .toList();
            if (destVendors.isEmpty()) continue;

            String destinations = destVendors.stream()
                    .map(v -> v.getLatitude() + "," + v.getLongitude())
                    .collect(Collectors.joining("|"));

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

            int mapped = 0, skipped = 0;

            // ✅ Map back using the SAME destVendors list (keeps indices aligned)
            for (int j = 0; j < destVendors.size() && j < elements.size(); j++) {
                Vendor v = destVendors.get(j);
                DistanceMatrixResponse.Element el = elements.get(j);

                if (el == null || el.status == null || !"OK".equalsIgnoreCase(el.status)
                        || el.distance == null || el.duration == null) {
                    skipped++;
                    continue;
                }

                long meters = el.distance.value;   // meters
                long seconds = el.duration.value;  // seconds

                // ✅ Only skip negatives (invalid). Zero is valid for identical coords.
                if (meters < 0 || seconds < 0) {
                    skipped++;
                    continue;
                }

                double km = meters / 1000.0;
                int minutes = (int) Math.ceil(seconds / 60.0);

                // ✅ Clamp for UX (identical points often come as 0m/0s)
                if (minutes < 1) minutes = 1;

                out.put(v.getId(), new DrivingInfo(km, minutes));
                mapped++;
            }

            // (Optional) quick visibility while testing
            // org.slf4j.LoggerFactory.getLogger(getClass())
            //     .debug("DM batch {}: dests={}, mapped={}, skipped={}", i / BATCH_SIZE, destVendors.size(), mapped, skipped);
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
