package com.project.stockexchangeappbackend.util;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.thread")
@Data
public class ThreadsProperties {

    private Integer stockProcessing;

}
