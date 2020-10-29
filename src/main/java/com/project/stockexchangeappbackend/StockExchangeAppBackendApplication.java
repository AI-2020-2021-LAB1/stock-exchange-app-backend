package com.project.stockexchangeappbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class StockExchangeAppBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockExchangeAppBackendApplication.class, args);
	}

}
