package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "CaroCartProductService")
public interface ProductClient {

    @GetMapping("/products/dto/{id}")
    ProductDTO getProductById(@PathVariable("id") Long productId);
    
    @PutMapping("/products/{id}/decrease-stock")
    void decreaseStock(@PathVariable("id") Long productId, @RequestParam("quantity") int quantity);
}
