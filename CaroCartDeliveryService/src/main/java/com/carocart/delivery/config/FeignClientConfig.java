package com.carocart.delivery.config;

import org.springframework.context.annotation.Bean;
import feign.Logger;

public class FeignClientConfig {
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
