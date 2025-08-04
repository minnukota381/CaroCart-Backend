package com.carocart.delivery.service;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

import com.carocart.delivery.util.JwtUtil;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    private final String internalToken;

    public FeignClientInterceptor(JwtUtil jwtUtil) {
        this.internalToken = "Bearer " + jwtUtil.generateToken(0L, "ADMIN", "System");
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header("Authorization", internalToken);
    }
}

