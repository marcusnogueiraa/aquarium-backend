package com.aquarium.infrastructure.configuration;

import com.aquarium.application.port.out.TemperatureRepositoryPort;
import com.aquarium.application.service.TemperatureService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public TemperatureService temperatureService(TemperatureRepositoryPort port) {
        return new TemperatureService(port);
    }
}