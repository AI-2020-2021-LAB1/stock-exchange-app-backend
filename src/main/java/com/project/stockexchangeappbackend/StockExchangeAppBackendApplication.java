package com.project.stockexchangeappbackend;

import com.project.stockexchangeappbackend.util.StockIndexTimeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableScheduling
@EnableConfigurationProperties({StockIndexTimeProperties.class})
public class StockExchangeAppBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockExchangeAppBackendApplication.class, args);
	}

}
