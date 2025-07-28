package com.carocart.home.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.carocart.home.dto.CategoryDTO;

@FeignClient(name = "product-service")
public interface CategoryClient {
    @GetMapping("/categories")
    List<CategoryDTO> getAllCategories();
}
