package com.carocart.address.service;

import com.carocart.address.DTO.AddressRequestDTO;
import com.carocart.address.DTO.AddressResponseDTO;
import com.carocart.address.DTO.UserResponseDTO;
import com.carocart.address.entity.Address;
import com.carocart.address.feign.InternalUserClient;
import com.carocart.address.feign.UserClient;
import com.carocart.address.repository.AddressRepository;

import feign.FeignException;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AddressServiceImpl implements AddressService {
    private final AddressRepository repository;
    private final UserClient userClient;
    private final InternalUserClient internalUserClient;

    // SYSTEM JWT for internal calls to Authentication service

@Value("${security.internal-system-token:}")
private String internalSystemToken;

    public AddressServiceImpl(AddressRepository repository,
                              UserClient userClient,
                              InternalUserClient internalUserClient) {
        this.repository = repository;
        this.userClient = userClient;
        this.internalUserClient = internalUserClient;
    }
    @PostConstruct
    public void checkInternalToken() {
      System.out.println("Address SYSTEM token present? " + (internalSystemToken != null && !internalSystemToken.isBlank()));
    }

    private AddressResponseDTO mapToDTO(Address address) {
        AddressResponseDTO dto = new AddressResponseDTO();
        dto.setId(address.getId());
        dto.setFullName(address.getFullName());
        dto.setPhoneNumber(address.getPhoneNumber());
        dto.setAddress(address.getAddress());
        dto.setLatitude(address.getLatitude());
        dto.setLongitude(address.getLongitude());
        dto.setType(address.getType());
        dto.setInstructions(address.getInstructions());
        dto.setIsDefault(address.getIsDefault());
        return dto;
    }

    /**
     * Push lat/lng to Users table if this address is default and we have a SYSTEM token.
     * Non-blocking: logs and continues on failure.
     */
    private void syncUserLatLng(Address address) {
        if (internalSystemToken == null || internalSystemToken.isBlank()) {
            System.out.println("WARN: internalSystemToken is blank; skipping user lat/lng sync");
            return;
        }
        if (address.getLatitude() == null || address.getLongitude() == null) return;

        System.out.printf("Syncing user %d lat=%f lng=%f%n",
                address.getUserId(), address.getLatitude(), address.getLongitude());
        try {
            internalUserClient.updateUserLocation(
                "Bearer " + internalSystemToken,
                address.getUserId(),
                Map.of("latitude", address.getLatitude(), "longitude", address.getLongitude())
            );
            System.out.println("✅ User lat/lng sync OK");
        } catch (feign.FeignException f) {
            System.out.println("❌ Sync failed: status=" + f.status() + " body=" + f.contentUTF8());
        } catch (Exception e) {
            System.out.println("❌ Sync failed: " + e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }
    @Override
    @Transactional
    public AddressResponseDTO createAddress(AddressRequestDTO dto, String token) {
        UserResponseDTO user = userClient.getCurrentUser(token);

        // 1) Unset current default(s)
        List<Address> userAddresses = repository.findByUserId(user.getId());
        for (Address a : userAddresses) {
            if (Boolean.TRUE.equals(a.getIsDefault())) {
                a.setIsDefault(false);
                repository.save(a);
            }
        }

        // 2) Save new address as default
        Address address = new Address();
        address.setUserId(user.getId());
        address.setFullName(dto.getFullName());
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setAddress(dto.getAddress());
        address.setLatitude(dto.getLatitude());
        address.setLongitude(dto.getLongitude());
        address.setType(dto.getType());
        address.setInstructions(dto.getInstructions());
        address.setIsDefault(true);

        Address saved = repository.save(address);
        // 3) Sync user's lat/lng from t

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public AddressResponseDTO updateAddress(Long id, AddressRequestDTO dto) {
        Address address = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Handle default toggle
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            List<Address> userAddresses = repository.findByUserId(address.getUserId());
            for (Address a : userAddresses) {
                if (!a.getId().equals(address.getId()) && Boolean.TRUE.equals(a.getIsDefault())) {
                    a.setIsDefault(false);
                    repository.save(a);
                }
            }
            address.setIsDefault(true);
        } else if (dto.getIsDefault() != null) {
            address.setIsDefault(false);
        }

        // Update fields
        address.setFullName(dto.getFullName());
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setAddress(dto.getAddress());
        address.setLatitude(dto.getLatitude());
        address.setLongitude(dto.getLongitude());
        address.setType(dto.getType());
        address.setInstructions(dto.getInstructions());

        Address saved = repository.save(address);
        // If it's now default, sync user'

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public void deleteAddress(Long id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getAllAddressesByUserId(Long userId) {
        List<Address> addresses = repository.findByUserId(userId);
        List<AddressResponseDTO> response = new ArrayList<>();
        for (Address address : addresses) {
            response.add(mapToDTO(address));
        }
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponseDTO getAddressById(Long id) {
        Address address = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        return mapToDTO(address);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponseDTO> getAddressesForCurrentUser(String token) {
        UserResponseDTO user = userClient.getCurrentUser(token);
        List<Address> addresses = repository.findByUserId(user.getId());
        List<AddressResponseDTO> response = new ArrayList<>();
        for (Address address : addresses) {
            response.add(mapToDTO(address));
        }
        return response;
    }
}
