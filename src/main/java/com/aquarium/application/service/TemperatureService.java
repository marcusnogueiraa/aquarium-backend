package com.aquarium.application.service;

import com.aquarium.application.domain.TemperatureReading;
import com.aquarium.application.port.in.RecordTemperatureUseCase;
import com.aquarium.application.port.in.QueryTemperatureUseCase;
import com.aquarium.application.port.out.RealTimeDataPort;
import com.aquarium.application.port.out.TemperatureRepositoryPort;
import java.time.Instant;
import java.util.List;

public class TemperatureService implements RecordTemperatureUseCase, QueryTemperatureUseCase {

    private final TemperatureRepositoryPort temperatureRepositoryPort;
    private final RealTimeDataPort realTimeDataPort;

    public TemperatureService(TemperatureRepositoryPort temperatureRepositoryPort, RealTimeDataPort realTimeDataPort) {
        this.temperatureRepositoryPort = temperatureRepositoryPort;
        this.realTimeDataPort = realTimeDataPort;
    }

    @Override
    public void recordTemperature(String aquariumId, Double value) {
        TemperatureReading reading = new TemperatureReading(aquariumId, value, Instant.now());
        temperatureRepositoryPort.save(reading);
        realTimeDataPort.publishTemperature(aquariumId, value);
    }

    @Override
    public List<TemperatureReading> getTemperatureReadings(String aquariumId, Instant start, Instant end) {
        return temperatureRepositoryPort.findByAquariumIdAndTimestampBetween(aquariumId, start, end);
    }
}