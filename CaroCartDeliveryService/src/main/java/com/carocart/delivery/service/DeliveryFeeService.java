// DeliveryFeeService.java
package com.carocart.delivery.service;

import com.carocart.delivery.dto.DeliveryFeeRequest;
import com.carocart.delivery.dto.DeliveryFeeResponse;
import com.carocart.delivery.util.DistanceUtil;
import org.springframework.stereotype.Service;

@Service
public class DeliveryFeeService {

    public DeliveryFeeResponse calculateFee(DeliveryFeeRequest request) {
        double distance = DistanceUtil.calculateDistance(
                request.getVendorLat(), request.getVendorLng(),
                request.getUserLat(), request.getUserLng()
        );

        // max distance rule
        double maxDistance = request.getVendorType().equalsIgnoreCase("FOOD") ? 12.0 : 8.0;
        if (distance > maxDistance) {
            return new DeliveryFeeResponse(distance, 0, false);
        }

        // fee slabs
        double fee;
        if (distance <= 2) fee = 19;
        else if (distance <= 5) fee = 29;
        else if (distance <= 8) fee = 39;
        else fee = 49;

        return new DeliveryFeeResponse(distance, fee, true);
    }
}
