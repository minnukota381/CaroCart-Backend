package com.carocart.authentication.service;

import com.carocart.authentication.dto.DeliveryBoyDTO;

import java.util.List;

public interface DeliveryBoyService {
    List<DeliveryBoyDTO> getDeliveryBoysByStatus(String status);
    void updateDeliveryBoyStatus(Long id, String status);
}
