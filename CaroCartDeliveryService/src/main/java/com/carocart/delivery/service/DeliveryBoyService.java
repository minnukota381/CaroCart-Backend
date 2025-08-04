package com.carocart.delivery.service;

import com.carocart.delivery.dto.DeliveryBoyDTO;
import com.carocart.delivery.dto.DeliveryBoyLoginDTO;
import com.carocart.delivery.dto.DeliveryBoyRegistrationDTO;
import com.carocart.delivery.entity.DeliveryBoy;
import org.springframework.http.ResponseEntity;
import java.util.List;

public interface DeliveryBoyService {
    DeliveryBoyDTO register(DeliveryBoyRegistrationDTO dto);

    DeliveryBoyDTO login(DeliveryBoyLoginDTO loginDTO);

    DeliveryBoyDTO getById(Long id);

    List<DeliveryBoyDTO> getAvailableDeliveryBoys();
    List<DeliveryBoyDTO> getDeliveryBoysByStatus(String status);
    void updateDeliveryBoyStatus(Long id, String status);
    void updateCurrentLocation(Long deliveryBoyId, Double lat, Double lng);
    ResponseEntity<String> sendForgotPasswordOtp(String phoneOrEmail);
    ResponseEntity<String> verifyForgotPasswordOtp(String phoneOrEmail, String otp);
    ResponseEntity<String> resetForgottenPassword(String phoneOrEmail, String password);

    DeliveryBoy findByEmail(String email);
    DeliveryBoy findByPhone(String phone);
}
