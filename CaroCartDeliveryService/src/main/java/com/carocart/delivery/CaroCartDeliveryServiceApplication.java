package com.carocart.delivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CaroCartDeliveryServiceApplication {
    public static void main(String[] args) {
    	System.out.println("Tomcat max file count: " + System.getProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.maxFileCount"));
        SpringApplication.run(CaroCartDeliveryServiceApplication.class, args);
    }
}
