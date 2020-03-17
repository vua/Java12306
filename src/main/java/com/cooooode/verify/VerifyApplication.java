package com.cooooode.verify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCircuitBreaker
@EnableScheduling
public class VerifyApplication {

    public static void main(String[] args) {

        SpringApplication.run(VerifyApplication.class, args);
    }

}
