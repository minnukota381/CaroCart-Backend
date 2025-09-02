package com.carocart.cartservice.feign;

import com.carocart.cartservice.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "CAROCARTPRODUCTSERVICE",       // Dev: Eureka
    url  = "${product.service.url:}",      // Prod: Cloud Run URL
    path = "/products"
)
public interface ProductClient {

    @GetMapping("/dto/{id}")
    ProductDTO getProductDTOById(@PathVariable("id") Long id);
}
