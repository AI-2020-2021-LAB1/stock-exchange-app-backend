package com.project.stockexchangeappbackend.configuration;

import com.project.stockexchangeappbackend.util.timemeasuring.ProcessingTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
public class CustomBeansConfiguration {

    @Bean
    @RequestScope
    public ProcessingTime responseObject() {
        return new ProcessingTime();
    }

}
