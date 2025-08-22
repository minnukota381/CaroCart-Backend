package com.carocart.delivery.service;

import com.carocart.delivery.dto.DeliveryEarningCreateRequest;
import com.carocart.delivery.dto.EarningsSummaryDTO;
import com.carocart.delivery.entity.DeliveryEarning;

import java.time.LocalDate;
import java.util.List;

public interface DeliveryEarningService {
    DeliveryEarning createInternal(DeliveryEarningCreateRequest req);
    List<DeliveryEarning> listMine(Long deliveryBoyId, LocalDate from, LocalDate to);
    EarningsSummaryDTO summary(Long deliveryBoyId, String period); // today|week|month
}
