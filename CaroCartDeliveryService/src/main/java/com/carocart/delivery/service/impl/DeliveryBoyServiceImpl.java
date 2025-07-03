package com.carocart.delivery.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.carocart.delivery.dto.DeliveryBoyDTO;
import com.carocart.delivery.dto.DeliveryBoyLoginDTO;
import com.carocart.delivery.entity.DeliveryBoy;
import com.carocart.delivery.util.PasswordUtil;
import com.carocart.delivery.repository.DeliveryBoyRepository;
import com.carocart.delivery.service.DeliveryBoyService;

@Service
public class DeliveryBoyServiceImpl implements DeliveryBoyService {

    @Autowired
    private DeliveryBoyRepository repo;

    @Override
    public DeliveryBoyDTO register(DeliveryBoyDTO dto, String rawPassword) {
        if (repo.findByPhone(dto.getPhone()).isPresent() || repo.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Phone or Email already registered");
        }
        DeliveryBoy entity = new DeliveryBoy();
        entity.setName(dto.getName());
        entity.setPhone(dto.getPhone());
        entity.setEmail(dto.getEmail());
        entity.setPasswordHash(PasswordUtil.hashPassword(rawPassword));
        entity.setActive(true);
        entity = repo.save(entity);
        return toDTO(entity);
    }

    @Override
    public DeliveryBoyDTO login(DeliveryBoyLoginDTO loginDTO) {
        DeliveryBoy user = repo.findByPhone(loginDTO.getPhoneOrEmail())
                .orElse(repo.findByEmail(loginDTO.getPhoneOrEmail())
                .orElseThrow(() -> new RuntimeException("User not found")));

        if (!PasswordUtil.verifyPassword(loginDTO.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        return toDTO(user);
    }

    @Override
    public DeliveryBoyDTO getById(Long id) {
        return repo.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Override
    public DeliveryBoyDTO getByPhoneOrEmail(String phoneOrEmail) {
        return repo.findByPhone(phoneOrEmail)
                .map(this::toDTO)
                .orElseGet(() ->
                    repo.findByEmail(phoneOrEmail)
                        .map(this::toDTO)
                        .orElseThrow(() -> new RuntimeException("User not found"))
                );
    }

    private DeliveryBoyDTO toDTO(DeliveryBoy entity) {
        DeliveryBoyDTO dto = new DeliveryBoyDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        return dto;
    }
}
