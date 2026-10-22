package com.smartvehicle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartVehicleApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartVehicleApplication.class, args);
    }
}
