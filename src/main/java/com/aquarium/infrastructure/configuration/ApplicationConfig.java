package com.aquarium.infrastructure.configuration;

import com.aquarium.application.port.out.PhRepositoryPort;
import com.aquarium.application.port.out.RealTimeDataPort;
import com.aquarium.application.port.out.TemperatureRepositoryPort;
import com.aquarium.application.service.PhService;
import com.aquarium.application.service.TemperatureService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public TemperatureService temperatureService(TemperatureRepositoryPort port, RealTimeDataPort realTimeDataPort) {
        return new TemperatureService(port, realTimeDataPort);
    }

    @Bean
    public PhService phService(PhRepositoryPort port, RealTimeDataPort realTimeDataPort) {
        return new PhService(port, realTimeDataPort);
    }
}