package com.carocart.delivery.service;

import com.carocart.delivery.dto.AdminEarningsPreviewDTO;
import com.carocart.delivery.dto.DeliveryEarningCreateRequest;
import com.carocart.delivery.dto.EarningsSummaryDTO;
import com.carocart.delivery.dto.DeliveryPayoutDTO;
import com.carocart.delivery.entity.DeliveryEarning;

import java.time.LocalDate;
import java.util.List;

public interface DeliveryEarningService {
    // Called internally by OrderService
    DeliveryEarning createInternal(DeliveryEarningCreateRequest req);

    // Rider
    List<DeliveryEarning> listMine(Long deliveryBoyId, LocalDate from, LocalDate to);
    EarningsSummaryDTO summary(Long deliveryBoyId, String period);

    // Admin
    AdminEarningsPreviewDTO previewUnpaid(Long deliveryBoyId);
    DeliveryPayoutDTO markPaid(Long deliveryBoyId, Long adminIdOrNull, String notes);
}
