package com.project.stockexchangeappbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableScheduling
public class StockExchangeAppBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockExchangeAppBackendApplication.class, args);
	}

}
