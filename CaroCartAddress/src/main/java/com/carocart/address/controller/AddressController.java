package com.carocart.address.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.carocart.address.DTO.AddressRequestDTO;
import com.carocart.address.DTO.AddressResponseDTO;
import com.carocart.address.service.AddressService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("api/addresses")
public class AddressController {

    private final AddressService service;

    @Autowired
    public AddressController(AddressService service) {
        this.service = service;
    }

    // --- Address CRUD endpoints ---

    @PostMapping
    public AddressResponseDTO create(@Valid @RequestBody AddressRequestDTO dto,
                                     @RequestHeader("Authorization") String token) {
        return service.createAddress(dto, token);
    }

    @PutMapping("/{id}")
    public AddressResponseDTO update(@PathVariable Long id,
                                     @Valid @RequestBody AddressRequestDTO dto) {
        return service.updateAddress(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteAddress(id);
    }

    @GetMapping("/user/{userId}")
    public List<AddressResponseDTO> getAllByUser(@PathVariable Long userId) {
        return service.getAllAddressesByUserId(userId);
    }

    @GetMapping("/{id}")
    public AddressResponseDTO getOne(@PathVariable Long id) {
        return service.getAddressById(id);
    }

    @GetMapping("/me")
    public List<AddressResponseDTO> getMyAddresses(@RequestHeader("Authorization") String token) {
        return service.getAddressesForCurrentUser(token);
    }
}
