package com.carocart.authentication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.carocart.authentication.feign")
public class CaroCartAuthenticationApplication {
    public static void main(String[] args) {
        SpringApplication.run(CaroCartAuthenticationApplication.class, args);
    }
}
