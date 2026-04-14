package com.example.pricingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GlobalPricingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(GlobalPricingSystemApplication.class, args);
	}
}