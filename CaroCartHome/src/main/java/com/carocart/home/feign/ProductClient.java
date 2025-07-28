package com.carocart.home.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.carocart.home.dto.ProductDTO;

@FeignClient(name = "CaroCartProductService")

public interface ProductClient {

    @GetMapping("/products/dto/{id}")
    ProductDTO getProductDTOById(@PathVariable("id") Long id);
}
