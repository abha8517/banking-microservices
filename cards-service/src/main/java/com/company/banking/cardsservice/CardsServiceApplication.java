package com.company.banking.cardsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CardsServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardsServiceApplication.class, args);
    }
}
