package com.carocart.delivery.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.carocart.delivery.service.FeignClientInterceptor;
import com.carocart.delivery.util.JwtUtil;

@Configuration
public class FeignClientConfig {
    @Bean
    public FeignClientInterceptor feignClientInterceptor(JwtUtil jwtUtil) {
        return new FeignClientInterceptor(jwtUtil);
    }
}

