package com.carocart.paymentservice.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.filter.RequestContextFilter;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestTokenBearerInterceptor() {
        return requestTemplate -> {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

            if (requestAttributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

                String token = request.getHeader("Authorization");

                if (token != null && !token.isEmpty()) {
                    requestTemplate.header("Authorization", token);
                }
            }
        };
    }
    
    
   
    @Bean
    public RequestContextFilter requestContextFilter() {
        return new RequestContextFilter();
    }
}
