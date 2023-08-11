package com.example.parkingmanager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties(prefix = "application.config")
public class ApplicationProperties {
    private String profile;
    private String url;
    private String notificationService;

}
