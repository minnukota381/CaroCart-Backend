package com.carocart.orderservice.feign;

import com.carocart.orderservice.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "CAROCARTPRODUCT",              // Local (Eureka)
    url  = "${product.service.url:}",      // Prod (Cloud Run)
    path = "/products",
    contextId = "productClient"
)
public interface ProductClient {

    @GetMapping("/dto/{id}")
    ProductDTO getProductById(@PathVariable("id") Long productId);

    @PutMapping("/{id}/decrease-stock")
    void decreaseStock(
        @PathVariable("id") Long productId,
        @RequestParam("quantity") int quantity
    );
}
