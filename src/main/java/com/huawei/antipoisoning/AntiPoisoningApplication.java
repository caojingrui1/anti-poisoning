package com.huawei.antipoisoning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AntiPoisoningApplication {
    public static void main(String[] args) {
        SpringApplication.run(AntiPoisoningApplication.class, args);
    }

}
