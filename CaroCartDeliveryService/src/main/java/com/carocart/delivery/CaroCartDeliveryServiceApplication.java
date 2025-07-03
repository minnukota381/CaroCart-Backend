package com.carocart.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CaroCartDeliveryServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CaroCartDeliveryServiceApplication.class, args);
    }
}
