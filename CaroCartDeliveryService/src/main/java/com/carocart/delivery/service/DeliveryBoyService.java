package com.carocart.delivery.service;

import com.carocart.delivery.dto.DeliveryBoyDTO;
import com.carocart.delivery.dto.DeliveryBoyLoginDTO;

public interface DeliveryBoyService {
    DeliveryBoyDTO register(DeliveryBoyDTO dto, String rawPassword);
    DeliveryBoyDTO login(DeliveryBoyLoginDTO loginDTO);
    DeliveryBoyDTO getById(Long id);
    DeliveryBoyDTO getByPhoneOrEmail(String phoneOrEmail);
}
