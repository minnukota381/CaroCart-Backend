package com.carocart.address.service;

import com.carocart.address.DTO.AddressRequestDTO;
import com.carocart.address.DTO.AddressResponseDTO;
import com.carocart.address.DTO.UserResponseDTO;
import com.carocart.address.entity.Address;
import com.carocart.address.feign.UserClient;
import com.carocart.address.repository.AddressRepository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class AddressServiceImpl implements AddressService {
    private final AddressRepository repository;
    private final UserClient userClient;

    public AddressServiceImpl(AddressRepository repository, UserClient userClient) {
        this.repository = repository;
        this.userClient = userClient;
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

    @Override
    public AddressResponseDTO createAddress(AddressRequestDTO dto, String token) {
        UserResponseDTO user = userClient.getCurrentUser(token);

        // 1. Set all user's addresses to non-default
        List<Address> userAddresses = repository.findByUserId(user.getId());
        for (Address a : userAddresses) {
            if (Boolean.TRUE.equals(a.getIsDefault())) {
                a.setIsDefault(false);
                repository.save(a); // update
            }
        }

        // 2. Save new address as default
        Address address = new Address();
        address.setUserId(user.getId());
        address.setFullName(dto.getFullName());
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setAddress(dto.getAddress());
        address.setLatitude(dto.getLatitude());
        address.setLongitude(dto.getLongitude());
        address.setType(dto.getType());
        address.setInstructions(dto.getInstructions());
        address.setIsDefault(true); // Always set new as default

        return mapToDTO(repository.save(address));
    }


    @Override
    public AddressResponseDTO updateAddress(Long id, AddressRequestDTO dto) {
        Address address = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Address not found"));

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            // Unset isDefault for all addresses of this user except this one
            List<Address> userAddresses = repository.findByUserId(address.getUserId());
            for (Address a : userAddresses) {
                if (!a.getId().equals(address.getId()) && Boolean.TRUE.equals(a.getIsDefault())) {
                    a.setIsDefault(false);
                    repository.save(a);
                }
            }
            address.setIsDefault(true);
        } else {
            address.setIsDefault(false);
        }

        address.setFullName(dto.getFullName());
        address.setPhoneNumber(dto.getPhoneNumber());
        address.setAddress(dto.getAddress());
        address.setLatitude(dto.getLatitude());
        address.setLongitude(dto.getLongitude());
        address.setType(dto.getType());
        address.setInstructions(dto.getInstructions());
        // address.setIsDefault(dto.getIsDefault()); // Already handled above

        return mapToDTO(repository.save(address));
    }

    @Override
    public void deleteAddress(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<AddressResponseDTO> getAllAddressesByUserId(Long userId) {
        List<Address> addresses = repository.findByUserId(userId);
        List<AddressResponseDTO> response = new ArrayList<>();
        for (Address address : addresses) {
            response.add(mapToDTO(address));
        }
        return response;
    }

    @Override
    public AddressResponseDTO getAddressById(Long id) {
        Address address = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        return mapToDTO(address);
    }
    
    @Override
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
