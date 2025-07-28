package com.carocart.authentication.service;

import com.carocart.authentication.dto.DeliveryBoyDTO;
import com.carocart.authentication.feign.DeliveryBoyFeignClient;
import com.carocart.authentication.service.DeliveryBoyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeliveryBoyServiceImpl implements DeliveryBoyService {

    @Autowired
    private DeliveryBoyFeignClient deliveryBoyFeignClient;

    @Override
    public List<DeliveryBoyDTO> getDeliveryBoysByStatus(String status) {
        return deliveryBoyFeignClient.getDeliveryBoysByStatus(status);
    }

    @Override
    public void updateDeliveryBoyStatus(Long id, String status) {
        deliveryBoyFeignClient.updateDeliveryBoyStatus(id, status);
    }
}

