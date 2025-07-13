package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CaroCartProductService", contextId = "ProductClient")
public interface ProductClient {

    @GetMapping("/products/dto/{id}")
    ProductDTO getProductById(@PathVariable("id") Long productId);
}
