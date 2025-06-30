package com.carocart.address.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.carocart.address.DTO.AddressRequestDTO;
import com.carocart.address.DTO.AddressResponseDTO;
import com.carocart.address.service.AddressService;
import com.carocart.address.util.MapmyIndiaTokenService;

import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("api/addresses")
public class AddressController {

    private final AddressService service;
    private final MapmyIndiaTokenService tokenService;

    @Autowired
    public AddressController(AddressService service, MapmyIndiaTokenService tokenService) {
        this.service = service;
        this.tokenService = tokenService;
    }

    @GetMapping("/reverse-geocode")
    public ResponseEntity<?> reverseGeocode(@RequestParam double lat, @RequestParam double lng) {
        String accessToken = tokenService.getAccessToken();
        String url = "https://apis.mapmyindia.com/advancedmaps/v1/" + accessToken +
                     "/rev_geocode?lat=" + lat + "&lng=" + lng;
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            return ResponseEntity.ok(resp.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Error fetching location: " + e.getMessage());
        }
    }
    
    @GetMapping("/geocode-forward")
    public ResponseEntity<?> forwardGeocode(@RequestParam String address) {
        String accessToken = tokenService.getAccessToken();
        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        String url = "https://atlas.mapmyindia.com/api/places/geocode?address=" + encodedAddress;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
        	
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return ResponseEntity.ok(resp.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Error fetching coordinates: " + e.getMessage());
        }
    }
    
    @GetMapping("/places-autocomplete")
    public ResponseEntity<?> placesAutocomplete(@RequestParam String query) {
        String accessToken = tokenService.getAccessToken();
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = "https://atlas.mapmyindia.com/api/places/search/json?query=" + encodedQuery;
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return ResponseEntity.ok(resp.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body("Error fetching suggestions: " + e.getMessage());
        }
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
