// src/main/java/com/carocart/paymentservice/config/FeignConfig.java
package com.carocart.paymentservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.*;

@Configuration
public class FeignConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String auth = attrs.getRequest().getHeader("Authorization");
                if (auth != null) {
                    template.header("Authorization", auth);
                }
            }
        };
    }
}
